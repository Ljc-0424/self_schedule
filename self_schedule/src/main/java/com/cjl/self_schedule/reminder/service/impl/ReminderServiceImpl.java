package com.cjl.self_schedule.reminder.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cjl.self_schedule.reminder.entity.ReminderRecord;
import com.cjl.self_schedule.reminder.mapper.ReminderRecordMapper;
import com.cjl.self_schedule.reminder.service.ReminderService;
import com.cjl.self_schedule.common.utils.EncryptionUtil;
import com.cjl.self_schedule.common.vo.PageResult;
import com.cjl.self_schedule.common.service.WeChatBotService;
import com.cjl.self_schedule.task.entity.Task;
import com.cjl.self_schedule.task.service.TaskService;
import com.cjl.self_schedule.auth.entity.User;
import com.cjl.self_schedule.auth.service.UserService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class ReminderServiceImpl implements ReminderService {

    @Autowired
    @Lazy
    private TaskService taskService;

    @Autowired
    private ReminderRecordMapper reminderRecordMapper;

    @Autowired
    private UserService userService;

    @Autowired(required = false)
    private WeChatBotService weChatBotService;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private com.cjl.self_schedule.common.service.SseService sseService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final String REMINDER_QUEUE_KEY = "reminder:queue";
    private static final int SCAN_INTERVAL_SECONDS = 30;
    private static final int SCAN_TIME_RANGE_HOURS = 2;

    private static final int EXPIRED_HOURS_BEFORE_DELETE = 24;
    private static final int READ_HOURS_BEFORE_DELETE = 168;
    private static final int DISMISSED_HOURS_BEFORE_DELETE = 24;

    private static final int MAX_RETRY_COUNT = 3;
    private static final int RETRY_INTERVAL_MINUTES = 5;

    private static final int MAX_QUERY_LIMIT = 100;

    @Value("${reminder.cleanup-enabled:true}")
    private boolean cleanupEnabled;

    private volatile boolean indexesCreated = false;

    private void ensureIndexes() {
        if (indexesCreated) return;
        indexesCreated = true;
        try {
            reminderRecordMapper.createIndexOnSendStatusAndTriggerTime();
            reminderRecordMapper.createIndexOnTaskIdAndTriggerTime();
            reminderRecordMapper.createIndexOnUserIdAndStatus();
            taskService.createReminderIndexes();
            log.info("提醒相关数据库索引创建完成");
        } catch (Exception e) {
            log.warn("创建数据库索引失败（可能已存在）: {}", e.getMessage());
        }
    }

    private volatile boolean redisQueueLoaded = false;

    private void loadRedisQueueIfNeeded() {
        if (redisQueueLoaded) return;
        redisQueueLoaded = true;
        try {
            List<ReminderRecord> pendingReminders = reminderRecordMapper.selectList(
                    new QueryWrapper<ReminderRecord>()
                            .eq("send_status", 0)
                            .eq("status", "PENDING"));

            int loaded = 0;
            for (ReminderRecord r : pendingReminders) {
                if (r.getTriggerTime() != null) {
                    long triggerEpoch = r.getTriggerTime().atZone(ZoneId.systemDefault()).toEpochSecond();
                    redisTemplate.opsForZSet().add(REMINDER_QUEUE_KEY, String.valueOf(r.getId()), triggerEpoch);
                    loaded++;
                }
            }

            if (loaded > 0) {
                log.info("从数据库加载 {} 条待发送提醒到Redis队列", loaded);
            }
        } catch (Exception e) {
            log.warn("加载Redis提醒队列失败，将由兜底扫描补偿: {}", e.getMessage());
        }
    }

    @Override
    @Scheduled(fixedDelay = 1000 * SCAN_INTERVAL_SECONDS)
    public void scanAndTriggerReminders() {
        ensureIndexes();
        loadRedisQueueIfNeeded();

        log.debug("开始扫描到期提醒...");

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime endTime = now.plusHours(SCAN_TIME_RANGE_HOURS);

        List<Task> tasks = taskService.getTasksNeedReminder(now, endTime);

        log.debug("扫描到 {} 个需要处理的任务", tasks.size());

        for (Task task : tasks) {
            if (task.getRemindTime() != null) {
                processRemindTime(task, now, endTime);
            }

            if (task.getDeadline() != null) {
                processDeadline(task, now, endTime);
            }
        }

        processRetryReminders(now);

        compensateMissedReminders(now);

        processExpiredRecurringTasks(now);

        log.debug("提醒扫描完成");
    }

    @Scheduled(fixedDelay = 1000)
    public void dispatchDueReminders() {
        long nowEpoch = LocalDateTime.now().atZone(ZoneId.systemDefault()).toEpochSecond();

        Set<String> dueIds = redisTemplate.opsForZSet().rangeByScore(REMINDER_QUEUE_KEY, 0, nowEpoch, 0, 50);
        if (dueIds == null || dueIds.isEmpty()) {
            return;
        }

        for (String reminderIdStr : dueIds) {
            redisTemplate.opsForZSet().remove(REMINDER_QUEUE_KEY, reminderIdStr);

            Long reminderId;
            try {
                reminderId = Long.parseLong(reminderIdStr);
            } catch (NumberFormatException e) {
                continue;
            }

            // 检查记录是否已存在且状态为 PENDING
            ReminderRecord record = refreshReminderRecord(reminderId);
            if (record == null || !"PENDING".equals(record.getStatus())) {
                log.debug("提醒记录不存在或状态已变化，跳过 - reminderId: {}", reminderId);
                continue;
            }

            // 更新状态为 SENT
            int updatedRows = reminderRecordMapper.updateSendStatus(reminderId, 0, 1, LocalDateTime.now());
            if (updatedRows == 0) {
                log.debug("提醒记录已被处理或状态已变化，跳过 - reminderId: {}", reminderId);
                continue;
            }

            sendReminderNotificationAsync(record);

            log.info("精确发送提醒 - reminderId: {}, taskId: {}, triggerTime: {}",
                    record.getId(), record.getTaskId(), record.getTriggerTime());
        }
    }

    private void processExpiredRecurringTasks(LocalDateTime now) {
        List<Task> expiredTasks = taskService.getExpiredRecurringTasks(now);

        if (expiredTasks.isEmpty()) {
            return;
        }

        log.debug("发现 {} 个过期的重复任务，自动创建下一次任务", expiredTasks.size());

        for (Task expiredTask : expiredTasks) {
            LocalDateTime nextTime = taskService.calculateNextRecurrence(expiredTask);

            if (nextTime != null) {
                Task nextTask = taskService.createNextRecurrence(expiredTask);

                if (nextTask != null) {
                    deleteTaskReminders(expiredTask.getId());
                    taskService.removeById(expiredTask.getId());

                    log.info("已为过期重复任务自动创建下一次任务并删除过期任务 - originalTaskId: {}, newTaskId: {}, remindTime: {}, deadline: {}",
                            expiredTask.getId(), nextTask.getId(), nextTask.getRemindTime(), nextTask.getDeadline());
                }
            } else {
                deleteTaskReminders(expiredTask.getId());
                expiredTask.setStatus(4);
                expiredTask.setUpdatedTime(LocalDateTime.now());
                taskService.updateById(expiredTask);
                log.info("重复任务已到达结束日期，标记为已归档 - taskId: {}", expiredTask.getId());
            }
        }
    }

    private void processRemindTime(Task task, LocalDateTime now, LocalDateTime endTime) {
        LocalDateTime remindTime = task.getRemindTime();

        if (remindTime == null) {
            return;
        }

        List<Integer> advanceMinutes = parseAdvanceMinutes(task.getReminderConfig());

        if (advanceMinutes.isEmpty()) {
            advanceMinutes.add(0);
        }

        for (Integer minutes : advanceMinutes) {
            LocalDateTime triggerTime = remindTime.minusMinutes(minutes);

            if (triggerTime.isBefore(now)) {
                if (remindTime.isBefore(now) || reminderRecordMapper.countByTaskAndTime(task.getId(), remindTime) > 0) {
                    continue;
                }
                triggerTime = remindTime;
            }

            if (triggerTime.isAfter(endTime)) {
                continue;
            }

            if (reminderRecordMapper.countByTaskAndTime(task.getId(), triggerTime) == 0) {
                createReminder(task.getId(), task.getUserId(), task.getTitle(),
                        remindTime, triggerTime, "REMIND_TIME");
            }
        }
    }

    public void updateTaskReminders(Task task) {
        deletePendingReminders(task.getId());

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime endTime = now.plusDays(30);

        if (task.getRemindTime() != null) {
            LocalDateTime remindTime = task.getRemindTime();

            if (!remindTime.isBefore(now)) {
                processRemindTime(task, now, endTime);
            }
        }

        if (task.getDeadline() != null) {
            LocalDateTime deadline = task.getDeadline();

            if (!deadline.isBefore(now)) {
                processDeadline(task, now, endTime);
            }
        }
    }

    private void deletePendingReminders(Long taskId) {
        List<ReminderRecord> records = reminderRecordMapper.selectList(
                new QueryWrapper<ReminderRecord>().eq("task_id", taskId).eq("status", "PENDING"));
        for (ReminderRecord r : records) {
            try {
                redisTemplate.opsForZSet().remove(REMINDER_QUEUE_KEY, String.valueOf(r.getId()));
            } catch (Exception e) {
                log.warn("从Redis移除提醒失败 - reminderId: {}, error: {}", r.getId(), e.getMessage());
            }
        }
        reminderRecordMapper.delete(new QueryWrapper<ReminderRecord>()
                .eq("task_id", taskId)
                .eq("status", "PENDING"));
    }

    @Override
    public void deleteTaskReminders(Long taskId) {
        List<ReminderRecord> records = reminderRecordMapper.selectList(
                new QueryWrapper<ReminderRecord>().eq("task_id", taskId));
        for (ReminderRecord r : records) {
            try {
                redisTemplate.opsForZSet().remove(REMINDER_QUEUE_KEY, String.valueOf(r.getId()));
            } catch (Exception e) {
                log.warn("从Redis移除提醒失败 - reminderId: {}, error: {}", r.getId(), e.getMessage());
            }
        }
        reminderRecordMapper.delete(new QueryWrapper<ReminderRecord>()
                .eq("task_id", taskId));
        log.info("已删除任务 {} 的所有提醒记录", taskId);
    }

    private void processDeadline(Task task, LocalDateTime now, LocalDateTime endTime) {
        LocalDateTime deadline = task.getDeadline();

        if (deadline == null || deadline.isBefore(now) || deadline.isAfter(endTime)) {
            return;
        }

        if (task.getRemindTime() != null && !task.getRemindTime().isBefore(now)) {
            log.debug("任务已有remindTime提醒，跳过deadline提醒 - taskId: {}", task.getId());
            return;
        }

        List<Integer> advanceMinutes = parseAdvanceMinutes(task.getReminderConfig());

        if (advanceMinutes.isEmpty()) {
            advanceMinutes.add(60);
        }

        for (Integer minutes : advanceMinutes) {
            LocalDateTime triggerTime = deadline.minusMinutes(minutes);

            if (triggerTime.isBefore(now)) {
                if (reminderRecordMapper.countByTaskAndTime(task.getId(), deadline) == 0) {
                    if (!deadline.isBefore(now)) {
                        createReminder(task.getId(), task.getUserId(), task.getTitle(),
                                deadline, deadline, "DEADLINE");
                    }
                }
                continue;
            }

            if (!triggerTime.isAfter(endTime)) {
                if (reminderRecordMapper.countByTaskAndTime(task.getId(), triggerTime) == 0) {
                    createReminder(task.getId(), task.getUserId(), task.getTitle(),
                            deadline, triggerTime, "DEADLINE");
                }
            }
        }
    }

    private List<Integer> parseAdvanceMinutes(String reminderConfig) {
        List<Integer> minutes = new ArrayList<>();

        if (reminderConfig == null || reminderConfig.isEmpty()) {
            return minutes;
        }

        try {
            JsonNode root = objectMapper.readTree(reminderConfig);
            if (root.has("enable") && root.get("enable").asBoolean()) {
                JsonNode advanceMinutesNode = root.get("advance_minutes");
                if (advanceMinutesNode != null && advanceMinutesNode.isArray()) {
                    for (JsonNode node : advanceMinutesNode) {
                        minutes.add(node.asInt());
                    }
                } else {
                    JsonNode advanceHoursNode = root.get("advance_hours");
                    if (advanceHoursNode != null && advanceHoursNode.isArray()) {
                        for (JsonNode node : advanceHoursNode) {
                            minutes.add(node.asInt() * 60);
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.warn("解析提醒配置失败: {}", e.getMessage());
        }

        return minutes;
    }

    @Override
    public void createReminder(Long taskId, Long userId, String taskTitle,
                               LocalDateTime taskTime, LocalDateTime triggerTime, String reminderType) {
        ReminderRecord record = new ReminderRecord();
        record.setTaskId(taskId);
        record.setUserId(userId);
        record.setTaskTitle(taskTitle);
        record.setTaskTime(taskTime);
        record.setTriggerTime(triggerTime);
        record.setReminderType(reminderType);
        record.setMessage(buildMessage(taskTitle, taskTime, reminderType));

        reminderRecordMapper.insert(record);

        try {
            long triggerEpoch = triggerTime.atZone(ZoneId.systemDefault()).toEpochSecond();
            redisTemplate.opsForZSet().add(REMINDER_QUEUE_KEY, String.valueOf(record.getId()), triggerEpoch);
        } catch (Exception e) {
            log.warn("写入Redis提醒队列失败，将由兜底扫描补偿 - reminderId: {}, error: {}", record.getId(), e.getMessage());
        }

        log.info("创建提醒记录 - taskId: {}, userId: {}, triggerTime: {}, currentTime: {}, reminderType: {}",
                taskId, userId, triggerTime, LocalDateTime.now(), reminderType);
    }

    private void sendReminderNotification(ReminderRecord record) {
        if (weChatBotService != null) {
            try {
                String userWebhookUrl = getUserWebhookUrl(record.getUserId());

                if (userWebhookUrl != null && !userWebhookUrl.isEmpty()) {
                    String formattedTime = record.getTaskTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
                    weChatBotService.sendTaskReminder(record.getTaskTitle(), formattedTime, record.getReminderType(), userWebhookUrl);
                    log.info("发送微信提醒成功 - taskId: {}, triggerTime: {}, userId: {}",
                            record.getTaskId(), record.getTriggerTime(), record.getUserId());
                } else {
                    log.debug("用户未配置webhook，标记为已发送（前端展示） - taskId: {}, userId: {}",
                            record.getTaskId(), record.getUserId());
                }
            } catch (Exception e) {
                log.warn("发送企业微信提醒失败 - taskId: {}, userId: {}, retryCount: {}, error: {}",
                        record.getTaskId(), record.getUserId(), record.getRetryCount(), e.getMessage());
                handleNotificationFailure(record);
                return;
            }
        }

        record.setStatus("SENT");
        record.setSendStatus(1);
        reminderRecordMapper.updateById(record);

        try {
            Map<String, Object> pushData = new java.util.HashMap<>();
            pushData.put("id", record.getId());
            pushData.put("taskId", record.getTaskId());
            pushData.put("taskTitle", record.getTaskTitle());
            pushData.put("triggerTime", record.getTriggerTime() != null ? record.getTriggerTime().toString() : null);
            pushData.put("taskTime", record.getTaskTime() != null ? record.getTaskTime().toString() : null);
            pushData.put("reminderType", record.getReminderType());
            pushData.put("message", buildMessage(record.getTaskTitle(), record.getTaskTime(), record.getReminderType()));
            sseService.sendToUser(record.getUserId(), "reminder", pushData);
        } catch (Exception e) {
            log.debug("SSE推送提醒失败（用户可能不在线）: {}", e.getMessage());
        }
    }

    @Async("taskExecutor")
    public void sendReminderNotificationAsync(ReminderRecord record) {
        sendReminderNotification(record);
    }

    private String getUserWebhookUrl(Long userId) {
        try {
            User user = userService.getById(userId);
            if (user != null && user.getWeChatWebhookUrl() != null) {
                return EncryptionUtil.decrypt(user.getWeChatWebhookUrl());
            }
        } catch (Exception e) {
            log.warn("获取用户webhook地址失败: {}", e.getMessage());
        }
        return null;
    }

    private void handleNotificationFailure(ReminderRecord record) {
        log.warn("提醒通知发送失败，标记为已过期，不再重试 - reminderId: {}, taskId: {}, retryCount: {}",
                record.getId(), record.getTaskId(), record.getRetryCount());
        record.setStatus("EXPIRED");
        record.setSendStatus(2);
        record.setUpdatedAt(LocalDateTime.now());
        reminderRecordMapper.updateById(record);
    }


    private ReminderRecord refreshReminderRecord(Long id) {
        try {
            return reminderRecordMapper.selectById(id);
        } catch (Exception e) {
            log.warn("刷新提醒记录失败 - reminderId: {}, error: {}", id, e.getMessage());
            return null;
        }
    }

    private void compensateMissedReminders(LocalDateTime now) {
        List<ReminderRecord> missed = reminderRecordMapper.findDueReminders(now, MAX_QUERY_LIMIT);
        if (missed.isEmpty()) {
            return;
        }

        log.warn("兜底补偿：发现 {} 条遗漏的到期提醒，可能是Redis异常导致", missed.size());

        for (ReminderRecord reminder : missed) {
            try {
                long triggerEpoch = reminder.getTriggerTime().atZone(ZoneId.systemDefault()).toEpochSecond();
                redisTemplate.opsForZSet().add(REMINDER_QUEUE_KEY, String.valueOf(reminder.getId()), triggerEpoch);
                log.info("兜底补偿：已将提醒重新加入Redis队列 - reminderId: {}, taskId: {}", reminder.getId(), reminder.getTaskId());
            } catch (Exception e) {
                log.warn("兜底补偿：写入Redis失败，跳过 - reminderId: {}, error: {}", reminder.getId(), e.getMessage());
            }
        }
    }

    private void processRetryReminders(LocalDateTime now) {
        LocalDateTime retryTimeThreshold = now.minusMinutes(RETRY_INTERVAL_MINUTES);
        List<ReminderRecord> retryReminders = reminderRecordMapper.findRetryReminders(retryTimeThreshold, MAX_RETRY_COUNT, now);

        for (ReminderRecord reminder : retryReminders) {
            if (reminder.getTriggerTime() != null && reminder.getTriggerTime().isAfter(now)) {
                log.debug("提醒时间还未到达，跳过重试 - reminderId: {}, triggerTime: {}",
                        reminder.getId(), reminder.getTriggerTime());
                continue;
            }

            int updatedRows = reminderRecordMapper.updateSendStatus(reminder.getId(), 0, 1, LocalDateTime.now());

            if (updatedRows == 0) {
                log.debug("提醒记录已被其他线程处理或状态已变化，跳过重试 - reminderId: {}", reminder.getId());
                continue;
            }

            ReminderRecord refreshedReminder = refreshReminderRecord(reminder.getId());
            if (refreshedReminder == null) {
                log.warn("刷新提醒记录失败，跳过重试 - reminderId: {}", reminder.getId());
                continue;
            }

            log.info("重试发送提醒 - reminderId: {}, retryCount: {}, triggerTime: {}",
                    refreshedReminder.getId(), refreshedReminder.getRetryCount(), refreshedReminder.getTriggerTime());
            sendReminderNotificationAsync(refreshedReminder);
        }
    }

    private String buildMessage(String taskTitle, LocalDateTime taskTime, String reminderType) {
        String timeStr = taskTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        LocalDateTime now = LocalDateTime.now();
        boolean isPast = taskTime.isBefore(now);

        if ("DEADLINE".equals(reminderType)) {
            if (isPast) {
                return String.format("任务「%s」已到期，截止时间：%s", taskTitle, timeStr);
            }
            return String.format("任务「%s」即将到期，截止时间：%s", taskTitle, timeStr);
        } else {
            if (isPast) {
                return String.format("任务「%s」已到时间：%s", taskTitle, timeStr);
            }
            return String.format("任务「%s」即将开始，时间：%s", taskTitle, timeStr);
        }
    }

    @Override
    public List<ReminderRecord> getPendingReminders(Long userId) {
        return reminderRecordMapper.findPendingByUserId(userId);
    }

    @Override
    public PageResult<ReminderRecord> getAllReminders(Long userId, Integer page, Integer pageSize) {
        int offset = (page - 1) * pageSize;
        List<ReminderRecord> records = reminderRecordMapper.findByUserId(userId, offset, pageSize);
        int total = reminderRecordMapper.countByUserId(userId);

        return PageResult.of(records, page, pageSize, (long) total);
    }

    @Override
    public PageResult<ReminderRecord> getRemindersByStatus(Long userId, String status, Integer page, Integer pageSize) {
        int offset = (page - 1) * pageSize;
        List<ReminderRecord> records = reminderRecordMapper.findByStatus(userId, status, offset, pageSize);
        int total = reminderRecordMapper.countByStatusTotal(userId, status);

        return PageResult.of(records, page, pageSize, (long) total);
    }

    @Override
    public void updateReminderStatus(Long id, String status) {
        ReminderRecord record = reminderRecordMapper.selectById(id);
        if (record != null) {
            record.setStatus(status);
            reminderRecordMapper.updateById(record);
        }
    }

    @Override
    public void markAllAsRead(Long userId) {
        reminderRecordMapper.updateAllStatusByUserId(userId, "READ");
    }

    @Override
    public void deleteReminder(Long id) {
        reminderRecordMapper.deleteById(id);
    }

    @Scheduled(cron = "0 0 2 * * ?")
    public void cleanupExpiredReminders() {
        if (!cleanupEnabled) {
            log.debug("提醒清理功能已禁用");
            return;
        }

        log.info("开始清理过期提醒记录...");
        LocalDateTime now = LocalDateTime.now();

        int deletedCount = 0;

        LocalDateTime expiredThreshold = now.minusHours(EXPIRED_HOURS_BEFORE_DELETE);
        int expiredDeleted = reminderRecordMapper.deleteByStatusAndTime("EXPIRED", expiredThreshold);
        deletedCount += expiredDeleted;
        if (expiredDeleted > 0) {
            log.info("清理已过期记录 {} 条", expiredDeleted);
        }

        LocalDateTime readThreshold = now.minusHours(READ_HOURS_BEFORE_DELETE);
        int readDeleted = reminderRecordMapper.deleteByStatusAndTime("READ", readThreshold);
        deletedCount += readDeleted;
        if (readDeleted > 0) {
            log.info("清理已读记录 {} 条", readDeleted);
        }

        LocalDateTime dismissedThreshold = now.minusHours(DISMISSED_HOURS_BEFORE_DELETE);
        int dismissedDeleted = reminderRecordMapper.deleteByStatusAndTime("DISMISSED", dismissedThreshold);
        deletedCount += dismissedDeleted;
        if (dismissedDeleted > 0) {
            log.info("清理已忽略记录 {} 条", dismissedDeleted);
        }

        int markedExpired = reminderRecordMapper.markExpiredReminders(now.minusHours(24));
        if (markedExpired > 0) {
            log.info("标记超时未读提醒为已过期 {} 条", markedExpired);
        }

        log.info("提醒清理完成，共清理 {} 条记录", deletedCount);
    }

    @Override
    public void markAsExpired(Long userId) {
        if (userId != null) {
            reminderRecordMapper.markExpiredByUserId(userId, LocalDateTime.now());
        } else {
            reminderRecordMapper.markExpiredReminders(LocalDateTime.now());
        }
    }

    @Override
    public int deleteByStatus(Long userId, List<String> status) {
        return reminderRecordMapper.deleteByStatus(userId, status);
    }

    @Override
    public ReminderStats getReminderStats(Long userId) {
        int pending = reminderRecordMapper.countByStatus(userId, "PENDING");
        int sent = reminderRecordMapper.countByStatus(userId, "SENT");
        int read = reminderRecordMapper.countByStatus(userId, "READ");
        int dismissed = reminderRecordMapper.countByStatus(userId, "DISMISSED");
        int expired = reminderRecordMapper.countByStatus(userId, "EXPIRED");
        int total = pending + sent + read + dismissed + expired;

        return new ReminderStats(total, pending, sent, read, dismissed, expired);
    }
}