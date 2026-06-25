package com.cjl.self_schedule.ai.service.impl;

import com.cjl.self_schedule.ai.service.AiTaskService;
import com.cjl.self_schedule.common.utils.EncryptionUtil;
import com.cjl.self_schedule.task.dto.TaskCreateDTO;
import com.cjl.self_schedule.task.entity.Task;
import com.cjl.self_schedule.task.service.TaskService;
import com.cjl.self_schedule.subtask.service.SubtaskService;
import com.cjl.self_schedule.auth.entity.User;
import com.cjl.self_schedule.auth.service.UserService;
import com.cjl.self_schedule.reminder.service.ReminderService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.Charset;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.LocalDate;
import java.util.*;

@Slf4j
@Service
public class AiTaskServiceImpl implements AiTaskService {

    private final TaskService taskService;
    private final UserService userService;
    private final ReminderService reminderService;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired(required = false)
    private SubtaskService subtaskService;

    @Autowired(required = false)
    private com.cjl.self_schedule.common.service.SseService sseService;

    @Value("${ai.zhipu.api-key:}")
    private String apiKey;

    @Value("${ai.zhipu.api-url:https://open.bigmodel.cn/api/paas/v4/chat/completions}")
    private String apiUrl;

    @Value("${ai.log-enabled:false}")
    private boolean aiLogEnabled;

    public AiTaskServiceImpl(@Lazy TaskService taskService, UserService userService,
            ReminderService reminderService, RestTemplate restTemplate) {
        this.taskService = taskService;
        this.userService = userService;
        this.reminderService = reminderService;
        this.restTemplate = restTemplate;
    }

    @Override
    public AiParseResult parseAndCreateTasks(Long userId, String prompt) {
        log.info("收到AI任务解析请求，userId: {}", userId);
        try {
            User user = resolveUser(userId);
            List<TaskCreateDTO> taskDTOs = parseTaskFromPrompt(prompt, user);
            log.info("AI解析完成，解析到任务数量: {}", taskDTOs.size());

            if (taskDTOs.isEmpty()) {
                return new AiParseResult("抱歉，我无法理解您的任务描述，请尝试用更清晰的方式表达。", false, 0, false);
            }

            return createTasksFromDtos(userId, taskDTOs);
        } catch (Exception e) {
            log.error("AI任务解析全链路异常: {}", e.getMessage(), e);
            String errorMsg = e.getMessage() != null && !e.getMessage().isEmpty()
                    ? e.getMessage() : "系统异常，请稍后重试";
            return new AiParseResult(errorMsg, false, 0, false);
        }
    }

    private User resolveUser(Long userId) {
        if (userId == null) return null;
        try {
            return userService.getById(userId);
        } catch (Exception e) {
            log.warn("获取用户信息失败，不影响AI解析: {}", e.getMessage());
            return null;
        }
    }

    private AiParseResult createTasksFromDtos(Long userId, List<TaskCreateDTO> taskDTOs) {
        int successCount = 0;
        List<String> createdTasks = new ArrayList<>();
        List<Task> savedTasks = new ArrayList<>();

        for (TaskCreateDTO dto : taskDTOs) {
            try {
                Task task = buildTaskFromDto(userId, dto);
                correctRemindTimeByRecurrenceRule(task);

                if (saveTaskWithRelatedData(task, dto)) {
                    successCount++;
                    createdTasks.add(dto.getTitle());
                    savedTasks.add(task);
                }
            } catch (Exception e) {
                log.error("单个任务保存失败，跳过该任务: {}", e.getMessage(), e);
            }
        }

        if (successCount == 0) {
            return new AiParseResult("任务创建失败，请稍后重试。", false, 0, false);
        }

        boolean isAiGenerated = taskDTOs.stream()
                .anyMatch(dto -> dto.getIsAiGenerated() != null && dto.getIsAiGenerated());

        String message = buildResultMessage(createdTasks, savedTasks);
        log.info("任务创建完成，successCount: {}, isAiGenerated: {}", successCount, isAiGenerated);

        if (sseService != null && successCount > 0) {
            try {
                Map<String, Object> pushData = new java.util.HashMap<>();
                pushData.put("type", "ai_task_created");
                pushData.put("taskCount", successCount);
                pushData.put("message", message);
                pushData.put("tasks", savedTasks.stream().map(t -> {
                    Map<String, Object> tMap = new java.util.HashMap<>();
                    tMap.put("id", t.getId());
                    tMap.put("title", t.getTitle());
                    tMap.put("remindTime", t.getRemindTime() != null ? t.getRemindTime().toString() : null);
                    tMap.put("deadline", t.getDeadline() != null ? t.getDeadline().toString() : null);
                    return tMap;
                }).collect(java.util.stream.Collectors.toList()));
                sseService.sendToUser(userId, "ai_task_created", pushData);
            } catch (Exception e) {
                log.debug("SSE推送AI任务创建通知失败: {}", e.getMessage());
            }
        }

        return new AiParseResult(message, true, successCount, isAiGenerated);
    }

    private Task buildTaskFromDto(Long userId, TaskCreateDTO dto) {
        Task task = new Task();
        task.setUserId(userId);
        String title = dto.getTitle();
        if (title != null && title.length() > 200) {
            title = title.substring(0, 200);
        }
        task.setTitle(title);
        task.setDescription(dto.getDescription());
        task.setPriority(dto.getPriority() != null ? dto.getPriority() : 2);
        task.setStatus(0);
        task.setDeadline(dto.getDeadline());
        task.setRemindTime(dto.getRemindTime());
        task.setEstimatedSeconds(dto.getEstimatedSeconds() != null ? dto.getEstimatedSeconds() : 0);
        task.setActualSeconds(0);
        task.setCategory(dto.getCategory() != null ? dto.getCategory() : "其他");
        if (dto.getTags() != null && !dto.getTags().isEmpty()) {
            try {
                task.setTags(new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(dto.getTags()));
            } catch (Exception e) {
                log.warn("tags序列化失败: {}", e.getMessage());
            }
        }
        task.setReminderConfig(dto.getReminderConfig() != null && !dto.getReminderConfig().isEmpty()
                ? dto.getReminderConfig() : null);
        task.setIsAiGenerated(dto.getIsAiGenerated() != null && dto.getIsAiGenerated() ? 1 : 0);
        task.setRecurrenceRule(dto.getRecurrenceRule());
        task.setRecurrenceEndDate(dto.getRecurrenceEndDate());
        task.setCreatedTime(LocalDateTime.now());
        task.setUpdatedTime(LocalDateTime.now());
        return task;
    }

    private boolean saveTaskWithRelatedData(Task task, TaskCreateDTO dto) {
        if (!taskService.save(task)) {
            return false;
        }
        log.info("任务创建成功 - taskId: {}, title: {}, remindTime: {}",
                task.getId(), task.getTitle(), task.getRemindTime());

        saveSubtasks(task.getId(), dto.getSubtasks());
        saveReminders(task, dto);
        return true;
    }

    private void saveSubtasks(Long taskId, List<String> subtaskTitles) {
        if (subtaskService == null || subtaskTitles == null || subtaskTitles.isEmpty()) return;
        for (String subtaskTitle : subtaskTitles) {
            try {
                subtaskService.create(taskId, subtaskTitle);
                log.info("子任务创建成功 - taskId: {}, subtaskTitle: {}", taskId, subtaskTitle);
            } catch (Exception e) {
                log.warn("创建子任务失败: {}", e.getMessage());
            }
        }
    }

    private void saveReminders(Task task, TaskCreateDTO dto) {
        if (reminderService == null) return;
        if (dto.getRemindTime() == null && dto.getDeadline() == null) return;
        try {
            reminderService.updateTaskReminders(task);
            log.info("为任务 {} 成功创建提醒记录", task.getId());
        } catch (Exception e) {
            log.warn("创建提醒记录失败，不影响任务创建: {}", e.getMessage());
        }
    }

    private String buildResultMessage(List<String> createdTasks, List<Task> savedTasks) {
        StringBuilder message = new StringBuilder();
        message.append(String.format("已成功为您创建 %d 个任务：\n\n", createdTasks.size()));
        for (int i = 0; i < savedTasks.size(); i++) {
            Task task = savedTasks.get(i);
            message.append(i + 1).append(". ").append(task.getTitle()).append("\n");

            String type = (task.getDeadline() != null && task.getRemindTime() != null) ? "任务型"
                    : (task.getRemindTime() != null) ? "事件型"
                    : (task.getDeadline() != null) ? "任务型" : "待办";
            message.append("   类型：").append(type);

            if (task.getDeadline() != null) {
                message.append("  |  截止：").append(formatDateTime(task.getDeadline()));
            }
            if (task.getRemindTime() != null) {
                message.append("  |  提醒：").append(formatDateTime(task.getRemindTime()));
            }
            message.append("\n");

            String priorityText = task.getPriority() != null
                    ? (task.getPriority() == 2 ? "高" : task.getPriority() == 1 ? "中" : "低") : "未设";
            message.append("   优先级：").append(priorityText);
            if (task.getCategory() != null && !task.getCategory().isEmpty()) {
                message.append("  |  分类：").append(task.getCategory());
            }
            if (task.getEstimatedSeconds() != null && task.getEstimatedSeconds() > 0) {
                message.append("  |  预估：").append(formatDuration(task.getEstimatedSeconds()));
            }
            message.append("\n");

            if (task.getRecurrenceRule() != null && !task.getRecurrenceRule().isEmpty()) {
                message.append("   重复：").append(task.getRecurrenceRule()).append("\n");
            }

            if (i < savedTasks.size() - 1) {
                message.append("\n");
            }
        }
        return message.toString();
    }

    private String formatDuration(int totalSeconds) {
        int h = totalSeconds / 3600;
        int m = (totalSeconds % 3600) / 60;
        if (h > 0 && m > 0) return h + "小时" + m + "分钟";
        if (h > 0) return h + "小时";
        if (m > 0) return m + "分钟";
        return totalSeconds + "秒";
    }

    @Override
    public List<TaskCreateDTO> parseTaskFromPrompt(String prompt) {
        return parseTaskFromPrompt(prompt, null);
    }

    @Override
    public List<TaskCreateDTO> parseTaskFromPrompt(String prompt, User user) {
        try {
            log.info("开始AI调用，prompt长度: {}", prompt != null ? prompt.length() : 0);

            String userApiKey = resolveApiKey(user);
            if (userApiKey == null || userApiKey.isEmpty()) {
                throw new RuntimeException("请先在个人信息中配置您的智谱API Key");
            }

            String responseBody = callAiApi(prompt, user, userApiKey);
            logAiResponseToFile(prompt, responseBody);

            if (responseBody == null) {
                throw new RuntimeException("AI返回响应体为空");
            }

            return parseAiResponse(responseBody);
        } catch (Exception e) {
            log.error("AI解析异常", e);
            throw new RuntimeException("AI解析失败: " + e.getMessage(), e);
        }
    }

    private String resolveApiKey(User user) {
        if (user == null || user.getAiApiKey() == null || user.getAiApiKey().isEmpty()) {
            return null;
        }
        return EncryptionUtil.decrypt(user.getAiApiKey());
    }

    private String callAiApi(String prompt, User user, String userApiKey) {
        Map<String, Object> requestBody = new LinkedHashMap<>();
        requestBody.put("model", "glm-4-flash");
        requestBody.put("temperature", 0.35f);
        requestBody.put("top_p", 0.7f);
        requestBody.put("max_tokens", 4096);
        requestBody.put("stream", false);
        requestBody.put("response_format", Map.of("type", "json_object"));

        List<Map<String, Object>> messages = new ArrayList<>();
        Map<String, Object> systemMsg = new LinkedHashMap<>();
        systemMsg.put("role", "system");
        systemMsg.put("content", buildSystemPrompt(user));
        messages.add(systemMsg);

        Map<String, Object> userMsg = new LinkedHashMap<>();
        userMsg.put("role", "user");
        userMsg.put("content", prompt);
        messages.add(userMsg);

        requestBody.put("messages", messages);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(userApiKey);
        headers.set("Content-Type", "application/json;charset=UTF-8");
        headers.set("Accept", "application/json;charset=UTF-8");

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        long startTime = System.currentTimeMillis();
        ResponseEntity<String> response = restTemplate.exchange(apiUrl, HttpMethod.POST, entity, String.class);
        long endTime = System.currentTimeMillis();
        log.info("AI调用完成，耗时: {}ms", endTime - startTime);

        return response.getBody();
    }

    private List<TaskCreateDTO> parseAiResponse(String responseBody) throws Exception {
        JsonNode rootNode = objectMapper.readTree(responseBody);

        if (rootNode.has("error")) {
            String errorMsg = rootNode.get("error").has("message")
                    ? rootNode.get("error").get("message").asText() : "Unknown error";
            log.error("AI调用失败: {}", errorMsg);
            throw new RuntimeException("AI调用失败: " + errorMsg);
        }

        JsonNode choices = rootNode.get("choices");
        if (choices == null || choices.isEmpty()) {
            throw new RuntimeException("AI返回格式错误，choices为空");
        }

        String aiContent = choices.get(0).get("message").get("content").asText();
        log.debug("AI解析内容: {}", aiContent);

        aiContent = cleanJsonResponse(aiContent);
        JsonNode aiJson = objectMapper.readTree(aiContent);
        JsonNode tasksNode = aiJson.get("tasks");

        if (tasksNode == null || !tasksNode.isArray()) {
            throw new RuntimeException("AI返回格式错误，tasks字段无效");
        }

        return parseTasksNode(tasksNode);
    }

    private List<TaskCreateDTO> parseTasksNode(JsonNode tasksNode) {
        List<TaskCreateDTO> taskDTOs = new ArrayList<>();
        for (JsonNode taskNode : tasksNode) {
            try {
                TaskCreateDTO dto = parseSingleTask(taskNode);
                taskDTOs.add(dto);
            } catch (Exception e) {
                log.warn("解析单个任务失败，跳过: {}", e.getMessage());
            }
        }
        return taskDTOs;
    }

    private TaskCreateDTO parseSingleTask(JsonNode taskNode) {
        TaskCreateDTO dto = new TaskCreateDTO();

        String title = taskNode.has("title") && !taskNode.get("title").isNull()
                ? taskNode.get("title").asText() : "未命名任务";
        if (title.matches("^\\d+$") || title.trim().isEmpty()) title = "未命名任务";
        dto.setTitle(title);

        dto.setDescription(taskNode.has("description") && !taskNode.get("description").isNull()
                ? taskNode.get("description").asText() : "");

        String recurrenceRule = taskNode.has("recurrenceRule") && !taskNode.get("recurrenceRule").isNull()
                ? taskNode.get("recurrenceRule").asText() : null;

        parseTaskTimes(taskNode, dto, recurrenceRule);

        dto.setPriority(taskNode.has("priority") && !taskNode.get("priority").isNull()
                ? taskNode.get("priority").asInt(2) : 2);
        dto.setCategory(taskNode.has("category") && !taskNode.get("category").isNull()
                ? taskNode.get("category").asText("其他") : "其他");

        dto.setTags(parseTags(taskNode));
        dto.setEstimatedSeconds(taskNode.has("estimatedSeconds")
                ? taskNode.get("estimatedSeconds").asInt(0) : 0);
        dto.setRecurrenceRule(recurrenceRule);

        parseRecurrenceEndDate(taskNode, dto);
        parseReminderConfig(taskNode, dto);
        dto.setIsAiGenerated(true);

        dto.setSubtasks(parseSubtasks(taskNode));

        log.info("解析任务成功 - title: {}, remindTime: {}, deadline: {}, subtasks: {}",
                dto.getTitle(), dto.getRemindTime(), dto.getDeadline(), dto.getSubtasks().size());

        return dto;
    }

    private void parseTaskTimes(JsonNode taskNode, TaskCreateDTO dto, String recurrenceRule) {
        if (taskNode.has("remindTime") && !taskNode.get("remindTime").isNull()) {
            try {
                LocalDateTime remindTime = LocalDateTime.parse(taskNode.get("remindTime").asText());
                dto.setRemindTime(remindTime);
            } catch (Exception e) {
                log.warn("提醒时间解析失败: {}", e.getMessage());
            }
        }

        if (taskNode.has("deadline") && !taskNode.get("deadline").isNull()) {
            try {
                LocalDateTime deadline = LocalDateTime.parse(taskNode.get("deadline").asText());
                deadline = adjustRemindTimeToFuture(deadline, recurrenceRule);
                dto.setDeadline(deadline);
            } catch (Exception e) {
                log.warn("截止时间解析失败: {}", e.getMessage());
            }
        }
    }

    private List<String> parseTags(JsonNode taskNode) {
        List<String> tags = new ArrayList<>();
        if (taskNode.has("tags") && taskNode.get("tags").isArray()) {
            for (JsonNode tagNode : taskNode.get("tags")) {
                String tag = tagNode.asText().trim();
                if (!tag.isEmpty() && !"null".equalsIgnoreCase(tag)) {
                    tags.add(tag);
                }
            }
        }
        return tags;
    }

    private List<String> parseSubtasks(JsonNode taskNode) {
        List<String> subtasks = new ArrayList<>();
        if (taskNode.has("subtasks") && taskNode.get("subtasks").isArray()) {
            for (JsonNode subtaskNode : taskNode.get("subtasks")) {
                String subtaskTitle = subtaskNode.asText().trim();
                if (!subtaskTitle.isEmpty()) {
                    subtasks.add(subtaskTitle);
                }
            }
        }
        return subtasks;
    }

    private void parseRecurrenceEndDate(JsonNode taskNode, TaskCreateDTO dto) {
        if (taskNode.has("recurrenceEndDate") && !taskNode.get("recurrenceEndDate").isNull()) {
            try {
                dto.setRecurrenceEndDate(LocalDateTime.parse(taskNode.get("recurrenceEndDate").asText()));
            } catch (Exception e) {
                log.warn("重复结束日期解析失败: {}", e.getMessage());
            }
        }
    }

    private void parseReminderConfig(JsonNode taskNode, TaskCreateDTO dto) {
        dto.setReminderConfig(taskNode.has("reminderConfig") && !taskNode.get("reminderConfig").isNull()
                ? taskNode.get("reminderConfig").asText()
                : "{\"enable\":false}");
    }

    private LocalDateTime adjustRemindTimeToFuture(LocalDateTime time, String recurrenceRule) {
        LocalDateTime now = LocalDateTime.now();

        if (time.isBefore(now)) {
            if (recurrenceRule != null && !recurrenceRule.isEmpty()) {
                if (recurrenceRule.startsWith("DAILY")) {
                    long daysToAdd = java.time.Duration.between(time, now).toDays() + 1;
                    time = time.plusDays(daysToAdd);
                    log.info("重复任务时间调整 - 原始时间: {}, 调整后: {}, 重复规则: {}",
                            time.minusDays(daysToAdd), time, recurrenceRule);
                } else if (recurrenceRule.startsWith("WEEKLY")) {
                    int todayOfWeek = now.getDayOfWeek().getValue();
                    int targetOfWeek = time.getDayOfWeek().getValue();
                    int daysDiff = targetOfWeek - todayOfWeek;
                    if (daysDiff <= 0) daysDiff += 7;
                    time = now.toLocalDate().plusDays(daysDiff).atTime(time.toLocalTime());
                    log.info("每周重复任务时间调整 - 原始时间: {}, 调整后: {}, 重复规则: {}",
                            time.minusDays(daysDiff), time, recurrenceRule);
                } else if (recurrenceRule.startsWith("MONTHLY")) {
                    time = time.plusMonths(1);
                    log.info("每月重复任务时间调整 - 原始时间: {}, 调整后: {}, 重复规则: {}",
                            time.minusMonths(1), time, recurrenceRule);
                } else {
                    time = time.plusDays(1);
                }
            } else {
                time = time.plusDays(1);
                log.info("非重复任务时间已过期，调整为明天: {}", time);
            }
        }

        return time;
    }

    private void correctRemindTimeByRecurrenceRule(Task task) {
        String recurrenceRule = task.getRecurrenceRule();
        LocalDateTime remindTime = task.getRemindTime();

        log.info("开始校正重复任务时间 - 标题: {}, remindTime: {}, rule: {}",
                task.getTitle(), remindTime, recurrenceRule);

        if (recurrenceRule == null || recurrenceRule.isBlank() || remindTime == null) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime correctedRemindTime = remindTime;

        if (recurrenceRule.startsWith("WEEKLY:")) {
            correctedRemindTime = correctWeeklyRecurrence(recurrenceRule, now, remindTime);
        } else if (recurrenceRule.startsWith("DAILY")) {
            if (remindTime.isBefore(now)) {
                correctedRemindTime = remindTime.plusDays(1);
                log.info("校正每日重复任务 - 原始: {}, 校正后: {}", remindTime, correctedRemindTime);
            }
        } else if (recurrenceRule.startsWith("MONTHLY:")) {
            correctedRemindTime = correctMonthlyRecurrence(recurrenceRule, now, remindTime);
        } else if (recurrenceRule.startsWith("YEARLY:")) {
            correctedRemindTime = correctYearlyRecurrence(recurrenceRule, now, remindTime);
        }

        if (!correctedRemindTime.equals(remindTime)) {
            task.setRemindTime(correctedRemindTime);
            syncDeadlineCorrection(task, remindTime, correctedRemindTime);
        }
    }

    private LocalDateTime correctWeeklyRecurrence(String rule, LocalDateTime now, LocalDateTime remindTime) {
        String[] parts = rule.split(":");
        if (parts.length < 3) return remindTime;
        try {
            String[] weekDays = parts[2].split(",");
            if (weekDays.length > 0 && !weekDays[0].isBlank()) {
                int targetWeekDay = Integer.parseInt(weekDays[0]);
                LocalDateTime corrected = findNextWeekday(now, targetWeekDay, remindTime.toLocalTime());
                log.info("校正每周重复任务 - 原始: {}, 规则: {}, 校正后: {}", remindTime, rule, corrected);
                return corrected;
            }
        } catch (NumberFormatException e) {
            log.warn("解析每周重复规则失败: {}", rule);
        }
        return remindTime;
    }

    private LocalDateTime correctMonthlyRecurrence(String rule, LocalDateTime now, LocalDateTime remindTime) {
        String[] parts = rule.split(":");
        if (parts.length < 3) return remindTime;
        try {
            int targetDay = Integer.parseInt(parts[2]);
            LocalDateTime corrected = findNextMonthlyDate(now, targetDay, remindTime.toLocalTime());
            log.info("校正每月重复任务 - 原始: {}, 规则: {}, 校正后: {}", remindTime, rule, corrected);
            return corrected;
        } catch (NumberFormatException e) {
            log.warn("解析每月重复规则失败: {}", rule);
        }
        return remindTime;
    }

    private LocalDateTime correctYearlyRecurrence(String rule, LocalDateTime now, LocalDateTime remindTime) {
        String[] parts = rule.split(":");
        if (parts.length < 3) return remindTime;
        try {
            String dateStr = parts[2];
            String[] dateParts = dateStr.split("-");
            if (dateParts.length == 2) {
                int targetMonth = Integer.parseInt(dateParts[0]);
                int targetDay = Integer.parseInt(dateParts[1]);
                LocalDateTime corrected = findNextYearlyDate(now, targetMonth, targetDay, remindTime.toLocalTime());
                log.info("校正每年重复任务 - 原始: {}, 规则: {}, 校正后: {}", remindTime, rule, corrected);
                return corrected;
            }
        } catch (NumberFormatException e) {
            log.warn("解析每年重复规则失败: {}", rule);
        }
        return remindTime;
    }

    private void syncDeadlineCorrection(Task task, LocalDateTime originalRemindTime, LocalDateTime correctedRemindTime) {
        if (task.getDeadline() != null && !task.getDeadline().isAfter(originalRemindTime)) {
            long secondsBetween = java.time.Duration.between(originalRemindTime, task.getDeadline()).getSeconds();
            LocalDateTime newDeadline = correctedRemindTime.plusSeconds(secondsBetween);
            task.setDeadline(newDeadline);
            log.info("同步校正deadline - 原始: {}, 校正后: {}", task.getDeadline(), newDeadline);
        }
    }

    private LocalDateTime findNextWeekday(LocalDateTime now, int targetWeekDay, LocalTime targetTime) {
        LocalDateTime next = now.plusSeconds(1);
        while (true) {
            if (next.getDayOfWeek().getValue() == targetWeekDay) {
                return next.with(targetTime);
            }
            next = next.plusDays(1);
        }
    }

    private LocalDateTime findNextMonthlyDate(LocalDateTime now, int targetDay, LocalTime targetTime) {
        int maxDay = now.getMonth().length(LocalDate.now().isLeapYear());
        int safeDay = Math.min(targetDay, maxDay);

        LocalDateTime candidate = now.plusSeconds(1).withDayOfMonth(safeDay).with(targetTime);

        if (!candidate.isBefore(now.plusSeconds(1))) {
            return candidate;
        }

        LocalDateTime nextMonth = now.plusMonths(1);
        int nextMaxDay = nextMonth.getMonth().length(nextMonth.toLocalDate().isLeapYear());
        int nextSafeDay = Math.min(targetDay, nextMaxDay);
        return nextMonth.withDayOfMonth(nextSafeDay).with(targetTime);
    }

    private LocalDateTime findNextYearlyDate(LocalDateTime now, int targetMonth, int targetDay, LocalTime targetTime) {
        int maxDay = java.time.YearMonth.of(now.getYear(), targetMonth).lengthOfMonth();
        int safeDay = Math.min(targetDay, maxDay);

        LocalDateTime candidate = now.plusSeconds(1).withMonth(targetMonth).withDayOfMonth(safeDay).with(targetTime);

        if (!candidate.isBefore(now.plusSeconds(1))) {
            return candidate;
        }

        int nextYear = now.getYear() + 1;
        int nextMaxDay = java.time.YearMonth.of(nextYear, targetMonth).lengthOfMonth();
        int nextSafeDay = Math.min(targetDay, nextMaxDay);
        return LocalDateTime.of(nextYear, targetMonth, nextSafeDay, targetTime.getHour(),
                targetTime.getMinute(), targetTime.getSecond());
    }

    private String buildSystemPrompt(User user) {
        String currentTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        StringBuilder userInfo = new StringBuilder("用户画像：");
        if (user != null) {
            if (user.getNickname() != null && !user.getNickname().isEmpty()) {
                userInfo.append("昵称：").append(user.getNickname()).append("；");
            } else if (user.getUsername() != null && !user.getUsername().isEmpty()) {
                userInfo.append("用户名：").append(user.getUsername()).append("；");
            }

            if (user.getOccupation() != null && !user.getOccupation().isEmpty()) {
                userInfo.append("职业：").append(user.getOccupation()).append("；");
            }

            if (user.getGender() != null && !user.getGender().isEmpty()) {
                userInfo.append("性别：").append(user.getGender()).append("；");
            }

            if (user.getCity() != null && !user.getCity().isEmpty()) {
                userInfo.append("所在城市：").append(user.getCity()).append("；");
            }

            if (user.getHobbies() != null && !user.getHobbies().isEmpty()) {
                userInfo.append("爱好：").append(user.getHobbies()).append("；");
            }

            if (user.getBio() != null && !user.getBio().isEmpty()) {
                userInfo.append("个人简介：").append(user.getBio()).append("；");
            }

            if (user.getBirthday() != null) {
                int age = LocalDateTime.now().getYear() - user.getBirthday().getYear();
                userInfo.append("年龄：").append(age).append("岁；");
            }

            userInfo.append("用户ID：").append(user.getId());
        } else {
            userInfo.append("匿名用户");
        }

        StringBuilder userTodoList = new StringBuilder();
        if (user != null) {
            try {
                List<Task> pendingTasks = taskService.queryTasks(user.getId(), null);
                if (pendingTasks != null && !pendingTasks.isEmpty()) {
                    int count = Math.min(pendingTasks.size(), 5);
                    for (int i = 0; i < count; i++) {
                        Task task = pendingTasks.get(i);
                        userTodoList.append((i + 1)).append(". ").append(task.getTitle());
                        if (task.getDeadline() != null) {
                            userTodoList.append("（截止：")
                                    .append(task.getDeadline().format(DateTimeFormatter.ofPattern("MM-dd HH:mm")))
                                    .append("）");
                        }
                        if (i < count - 1) userTodoList.append("；");
                    }
                    if (pendingTasks.size() > 5) {
                        userTodoList.append("；还有").append(pendingTasks.size() - 5).append("个待办");
                    }
                } else {
                    userTodoList.append("暂无待办任务");
                }
            } catch (Exception e) {
                userTodoList.append("获取待办任务失败");
            }
        } else {
            userTodoList.append("匿名用户，无法获取待办任务");
        }

        return new StringBuilder()
                .append("你是智能任务管家。精准理解用户口语化需求，生成合规JSON任务数据。\n\n")
                .append("=== 输出格式规范（最高优先级，违反即无效） ===\n")
                .append("仅输出纯JSON，禁止任何markdown、代码块、注释、解释、问候、多余字符。\n")
                .append("字段名与下方模板严格一致，大小写不能改。数字不加引号，null为原生小写null非字符串，字符串用英文双引号，数组末尾无多余逗号。\n")
                .append("时间字段统一yyyy-MM-ddTHH:mm:ss格式（如2026-05-18T23:59:59），禁止毫秒、时区后缀。截止时间默认23:59:59，禁止00:00。\n")
                .append("reminderConfig是JSON字符串，内部双引号转义一次：\\\"key\\\":value\n\n")
                .append("=== 上下文 ===\n")
                .append("当前时间：").append(currentTime).append("\n")
                .append(userInfo).append("\n")
                .append("待办参考：").append(userTodoList).append("\n\n")
                .append("=== 一、任务类型与字段规则（互斥，禁止交叉） ===\n")
                .append("判断标准：用户是否需要在某个时间点「交付成果」或「完成某件事」？\n")
                .append("- 事件型（仅提醒，无交付要求）：用户只需「到点去做」，不需要交付任何成果\n")
                .append("  典型场景：参加会议、上课、约会、聚餐、面试、看医生、接人、值班、起床、上班、健身、生日、纪念日、有课、去上课、去上班、去工作\n")
                .append("  关键词特征：「参加X」「去X」「到X」「X点有X」「提醒我X」「有课」「有X」「要去X」「我要去X」「我得去X」\n")
                .append("  重要判断：用户说「我去X」「我要去X」「X点去X」→一定是事件型，deadline必须为null\n")
                .append("  → remindTime=事件触发时间，deadline=null，estimatedSeconds=0\n")
                .append("- 任务型（有交付，有截止）：用户需要「完成某件事」，有明确的完成标准和截止期限\n")
                .append("  典型场景：写作业、交报告、完成毕设、考试复习、提交材料、完成项目\n")
                .append("  关键词特征：「完成X」「交X」「做完X」「X前搞定」「截止X」\n")
                .append("  → deadline=截止时间(默认23:59:59)，remindTime=提前计算的提醒时间(须<deadline)，须设estimatedSeconds\n")
                .append("- 复合型（触发时间+前置交付）：既有固定触发又有交付期限\n")
                .append("  典型场景：「答辩前先交论文」「开会前先准备方案」\n")
                .append("  → remindTime=事件触发时间，deadline=交付截止时间(须<remindTime)\n")
                .append("- 无时间待办：remindTime=null，deadline=null\n\n")
                .append("=== 二、时间计算规则 ===\n")
                .append("1. 时间解析（优先级从高到低）：\n")
                .append("   相对时间：'X分钟后'→当前+X分钟；'X小时后'→当前+X小时；'X天后'→当前+X天\n")
                .append("   固定时间：'X点/X:XX'→当日或次日对应时间\n")
                .append("   模糊时间：'早上'→08:00；'下午'→14:00；'晚上'→20:00\n")
                .append("   截止时间：'X天后截止'→当前+X天的23:59:59；'X号前'→当月X号23:59:59\n")
                .append("   所有生成的时间必须晚于当前时间\n")
                .append("2. 周期事件：用户说「每周X/每月X号」时，remindTime必须是当前时间之后第一个匹配日期，禁止与recurrenceRule不匹配\n")
                .append("3. 预估时长estimatedSeconds：根据用户描述和任务复杂度灵活判断（详见第五节第4条）\n")
                .append("4. 提醒时间remindTime智能计算：\n")
                .append("   核心公式：remindTime = deadline - 预估时长 - 缓冲时间\n")
                .append("   缓冲规则：<1天任务→2h；1-3天任务→12h；≥3天任务→24h\n")
                .append("   示例：预估3天+截止5月20日→remindTime应为5月17日（提前3天+缓冲），而非5月19日\n")
                .append("   灵活调整：\n")
                .append("   - 用户说「现在不想做」→可适当推迟提醒，但必须保证remindTime到deadline之间≥预估时长，确保能做完\n")
                .append("   - 用户说「尽快提醒」→remindTime设为当前时间之后不久，提前进入执行状态\n")
                .append("   - 用户说「不急」→可将remindTime推迟到deadline前刚好够完成的时间点\n")
                .append("   - 仅有截止时间未提需求→按公式自动计算，保证用户有充足时间\n")
                .append("   禁止：remindTime与deadline间隔小于预估时长（做不完）；remindTime设为deadline同一时刻（等于没提醒）\n")
                .append("5. reminderConfig：事件型→{\"enable\":false}；任务型仅当deadline与remindTime间隔>2h时可设提前提醒；无需求→{\"enable\":false}\n\n")
                .append("=== 三、重复规则（后端兼容格式，无重复则recurrenceRule=null，recurrenceEndDate=null） ===\n")
                .append("格式：TYPE:INTERVAL[:PARAMS]，INTERVAL默认1可省略\n")
                .append("每天→DAILY  每2天→DAILY:2\n")
                .append("每周→WEEKLY  每2周→WEEKLY:2  每周二→WEEKLY:1:2  每周一三→WEEKLY:1:1,3\n")
                .append("每月→MONTHLY  每2月→MONTHLY:2  每月1号→MONTHLY:1:1\n")
                .append("每年→YEARLY  每2年→YEARLY:2  每年5月1号→YEARLY:1:05-01\n\n")
                .append("=== 四、子任务规则 ===\n")
                .append("仅多步骤大任务才拆子任务（用户用逗号/顿号/步骤词分隔的并列项，或毕设论文等大型任务拆3-5步）。简单任务subtasks=[]。多个独立主任务拆为tasks数组多个对象。\n\n")
                .append("=== 五、个性化任务规划（核心能力，结合用户画像灵活安排） ===\n")
                .append("1. 智能提醒时间：根据任务难度和用户习惯灵活设置，而非机械计算。例如：\n")
                .append("   - 学生的考试复习→提前3-7天开始提醒，每天提醒一次，避免临时抱佛脚\n")
                .append("   - 上班族的项目交付→提前2-3天提醒开始准备，预留缓冲时间\n")
                .append("   - 日常习惯类（健身、读书）→固定时间提醒，培养规律\n")
                .append("   - 紧急事务（今天截止）→立即提醒，不设缓冲\n")
                .append("2. 智能优先级：综合判断，而非只看时间：\n")
                .append("   - 任务重要性：考试、面试、毕设答辩→高；日常学习、工作汇报→中；生活琐事→低\n")
                .append("   - 用户情绪：「急死了」「必须今天」「很重要」→高；「不急」「慢慢来」→低\n")
                .append("   - 时间紧迫度：24h内→高；3-7天→中；>7天→低（但重要任务可提前提升）\n")
                .append("   - 用户负荷：用户待办多时，非紧急任务降级；用户空闲时，长期任务可升级\n")
                .append("3. 智能分类与标签：深度理解用户的口语表达和个人场景：\n")
                .append("   - 从用户原话中提取关键词作为分类依据，而非生硬套模板\n")
                .append("   - 学生说\"搓毕设\"→分类\"毕业设计\"，标签[\"毕设\",\"毕业\",\"论文\"]\n")
                .append("   - 上班族说\"周报\"→分类\"工作汇报\"，标签[\"周报\",\"工作\"]\n")
                .append("   - 用户说\"去医院复查\"→分类\"健康管理\"，标签[\"医疗\",\"复查\"]\n")
                .append("   - 分类要精准反映任务核心属性，标签要能帮助用户检索和归类\n")
                .append("4. 智能预估时长：结合用户描述判断复杂度，而非固定范围：\n")
                .append("   - 用户说「很快搞定」「小活」→15-30分钟\n")
                .append("   - 用户说「有点麻烦」「需要想想」→2-4小时\n")
                .append("   - 用户说「大工程」「搞一周」→按用户描述估算\n")
                .append("   - 未明确描述→根据任务类型+用户职业合理推断\n")
                .append("5. 智能description：还原用户的核心需求，而非重复标题：\n")
                .append("   - 有明确描述→保留用户原意，适当补充上下文\n")
                .append("   - 无明确描述→根据标题和场景生成简洁说明\n")
                .append("   - 示例：标题\"写论文\"→描述\"完成毕业论文撰写，包含文献综述、正文、参考文献\"\n")
                .append("6. 智能reminderConfig提前提醒：根据任务紧迫度灵活设置：\n")
                .append("   - 紧急任务（<1天）→不设提前提醒，remindTime就是执行时间\n")
                .append("   - 中期任务（1-7天）→可设提前1-2小时提醒，让用户有心理准备\n")
                .append("   - 长期任务（>7天）→可设提前1天提醒，避免遗忘\n")
                .append("   - 用户说「提前一天提醒我」→按用户要求设置\n")
                .append("7. 智能子任务拆分：大型任务按实际执行流程拆分，而非机械拆分：\n")
                .append("   - 用户说\"搞毕设\"→[\"确定选题\",\"文献综述\",\"系统设计\",\"编码实现\",\"撰写论文\",\"答辩准备\"]\n")
                .append("   - 用户说\"准备面试\"→[\"复习基础知识\",\"刷题\",\"准备项目介绍\",\"模拟面试\"]\n")
                .append("   - 简单任务不要拆，避免给用户增加负担\n\n")
                .append("=== 六、优先级、分类、标签 ===\n")
                .append("priority(数字无引号)：\n")
                .append("  高(2)：任务重要且时间紧迫（考试/面试/毕设/24h内DDL/用户说「急」「重要」）\n")
                .append("  中(1)：任务有一定重要性或3-7天内截止（日常工作/学习/有期限的事项）\n")
                .append("  低(0)：日常琐事、无紧迫感、可延后（生活/娱乐/习惯养成）\n")
                .append("  判断时综合考虑：任务重要性 > 时间紧迫度 > 用户情绪 > 用户负荷\n")
                .append("category：从用户原话中提取核心场景，如\"搓项目\"→\"个人项目\"，\"学习408\"→\"考研备考\"，\"去医院\"→\"健康管理\"\n")
                .append("tags：2-4个精准标签，从任务内容+用户场景中提取，帮助用户检索归类\n\n")
                .append("=== 输出模板（字段顺序不能变） ===\n")
                .append("{\n")
                .append("  \"tasks\": [\n")
                .append("    {\n")
                .append("      \"title\": \"任务标题\",\n")
                .append("      \"description\": \"任务描述\",\n")
                .append("      \"remindTime\": \"ISO8601或null\",\n")
                .append("      \"deadline\": \"ISO8601或null\",\n")
                .append("      \"priority\": 0,\n")
                .append("      \"category\": \"分类\",\n")
                .append("      \"tags\": [\"标签1\",\"标签2\"],\n")
                .append("      \"estimatedSeconds\": 86400,\n")
                .append("      \"recurrenceRule\": \"重复规则或null\",\n")
                .append("      \"recurrenceEndDate\": \"ISO8601或null\",\n")
                .append("      \"reminderConfig\": \"{\\\"enable\\\":false}\",\n")
                .append("      \"subtasks\": [\"子任务1\",\"子任务2\"]\n")
                .append("    }\n")
                .append("  ]\n")
                .append("}")
                .toString();
    }

    private String cleanJsonResponse(String rawResponse) {
        if (rawResponse == null) return null;
        String cleaned = rawResponse.replaceAll("`json\\s*|`", "").trim();
        int start = cleaned.indexOf("{");
        int end = cleaned.lastIndexOf("}");
        if (start != -1 && end != -1 && end > start) {
            return cleaned.substring(start, end + 1);
        }
        return cleaned;
    }

    private String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null) return "";
        return dateTime.format(DateTimeFormatter.ofPattern("yyyy年MM月dd日 HH:mm"));
    }

    private void logAiResponseToFile(String prompt, String responseBody) {
        if (!aiLogEnabled) return;
        try {
            java.io.File logDir = new java.io.File("ai_logs");
            if (!logDir.exists()) {
                logDir.mkdirs();
            }

            String fileName = "ai_logs/ai_response_" + LocalDateTime.now().format(
                    DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".json";

            java.util.Map<String, Object> logEntry = new java.util.LinkedHashMap<>();
            logEntry.put("timestamp", LocalDateTime.now().toString());
            logEntry.put("prompt", prompt);
            logEntry.put("response", responseBody);

            String logContent = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(logEntry);
            java.io.FileWriter writer = new java.io.FileWriter(fileName, Charset.defaultCharset());
            writer.write(logContent);
            writer.close();

            log.info("AI返回数据已记录到文件: {}", fileName);
        } catch (Exception e) {
            log.warn("记录AI返回数据到文件失败: {}", e.getMessage());
        }
    }
}
