package com.cjl.self_schedule.common.converter;

import com.cjl.self_schedule.auth.entity.User;
import com.cjl.self_schedule.auth.vo.LoginVO;
import com.cjl.self_schedule.auth.vo.UserVO;
import com.cjl.self_schedule.common.utils.EncryptionUtil;
import com.cjl.self_schedule.focus.entity.FocusRecord;
import com.cjl.self_schedule.focus.vo.FocusRecordVO;
import com.cjl.self_schedule.task.entity.Task;
import com.cjl.self_schedule.task.service.TaskService;
import com.cjl.self_schedule.task.vo.TaskVO;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class VOConverter {

    @Autowired
    private TaskService taskService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 转换为登录响应 VO
     */
    public LoginVO convertToLoginVO(User user, String token) {
        LoginVO loginVO = new LoginVO();
        loginVO.setToken(token);
        loginVO.setUser(convertToUserVO(user));
        return loginVO;
    }

    public TaskVO convertToTaskVO(Task task) {
        TaskVO taskVO = new TaskVO();
        taskVO.setId(task.getId());
        taskVO.setUserId(task.getUserId());
        taskVO.setTitle(task.getTitle());
        taskVO.setDescription(task.getDescription());
        taskVO.setPriority(task.getPriority());
        taskVO.setStatus(task.getStatus());
        taskVO.setDeadline(task.getDeadline());
        taskVO.setRemindTime(task.getRemindTime());
        taskVO.setEstimatedSeconds(task.getEstimatedSeconds());
        taskVO.setActualSeconds(task.getActualSeconds());
        taskVO.setCategory(task.getCategory());
        
        taskVO.setTags(parseTags(task.getTags()));
        
        taskVO.setReminderConfig(task.getReminderConfig());
        taskVO.setIsAiGenerated(task.getIsAiGenerated() != null && task.getIsAiGenerated() == 1);
        taskVO.setRecurrenceRule(task.getRecurrenceRule());
        taskVO.setRecurrenceEndDate(task.getRecurrenceEndDate());
        taskVO.setCreatedTime(task.getCreatedTime());
        taskVO.setUpdatedTime(task.getUpdatedTime());
        taskVO.setCompletedTime(task.getCompletedTime());
        return taskVO;
    }

    public TaskVO convertToSimpleTaskVO(Task task) {
        TaskVO taskVO = new TaskVO();
        taskVO.setId(task.getId());
        taskVO.setTitle(task.getTitle());
        taskVO.setEstimatedSeconds(task.getEstimatedSeconds());
        taskVO.setDeadline(task.getDeadline());
        return taskVO;
    }

    public UserVO convertToUserVO(User user) {
        UserVO userVO = new UserVO();
        userVO.setId(user.getId());
        userVO.setUsername(user.getUsername());
        userVO.setNickname(user.getNickname());
        userVO.setEmail(user.getEmail());
        userVO.setAvatarUrl(user.getAvatarUrl());
        userVO.setSettings(user.getSettings());
        userVO.setIsActive(user.getIsActive());
        userVO.setStatus(user.getStatus());
        userVO.setRole(user.getRole());
        userVO.setLastLoginTime(user.getLastLoginTime());
        userVO.setOccupation(user.getOccupation());
        userVO.setPhone(user.getPhone());
        userVO.setGender(user.getGender());
        userVO.setCity(user.getCity());
        userVO.setHobbies(user.getHobbies());
        userVO.setBio(user.getBio());
        userVO.setWeChatWebhookUrl(EncryptionUtil.decrypt(user.getWeChatWebhookUrl()));
        userVO.setAiApiKey(EncryptionUtil.decrypt(user.getAiApiKey()));
        userVO.setCreatedTime(user.getCreatedTime());
        userVO.setUpdatedTime(user.getUpdatedTime());
        userVO.setBirthday(user.getBirthday());
        userVO.setDailyFocusGoal(user.getDailyFocusGoal());
        userVO.setMinEffectiveDuration(user.getMinEffectiveDuration());
        return userVO;
    }

    public FocusRecordVO convertToFocusRecordVO(FocusRecord record) {
        FocusRecordVO vo = new FocusRecordVO();
        vo.setId(record.getId());
        vo.setUserId(record.getUserId());
        vo.setTaskId(record.getTaskId());

        if (record.getTaskId() != null) {
            Task task = taskService.getById(record.getTaskId());
            if (task != null) {
                vo.setTaskTitle(task.getTitle());
            }
        }

        vo.setStartTime(record.getStartTime());
        vo.setEndTime(record.getEndTime());
        vo.setDuration(record.getDuration());
        vo.setInterruptions(record.getInterruptions());
        vo.setFocusScore(record.getFocusScore());
        vo.setNotes(record.getNotes());
        vo.setStatus(record.getStatus());
        vo.setCreatedTime(record.getCreatedTime());
        return vo;
    }

    /**
     * 批量转换 FocusRecordVO，预加载关联的 Task，避免 N+1 查询
     */
    public List<FocusRecordVO> convertToFocusRecordVOList(List<FocusRecord> records) {
        if (records == null || records.isEmpty()) {
            return Collections.emptyList();
        }

        // 批量查询关联的 Task
        Set<Long> taskIds = records.stream()
                .map(FocusRecord::getTaskId)
                .filter(id -> id != null)
                .collect(Collectors.toSet());

        Map<Long, Task> taskMap = Collections.emptyMap();
        if (!taskIds.isEmpty()) {
            List<Task> tasks = taskService.listByIds(taskIds);
            taskMap = tasks.stream().collect(Collectors.toMap(Task::getId, t -> t, (a, b) -> a));
        }

        final Map<Long, Task> finalTaskMap = taskMap;
        return records.stream()
                .map(record -> {
                    FocusRecordVO vo = new FocusRecordVO();
                    vo.setId(record.getId());
                    vo.setUserId(record.getUserId());
                    vo.setTaskId(record.getTaskId());

                    if (record.getTaskId() != null) {
                        Task task = finalTaskMap.get(record.getTaskId());
                        if (task != null) {
                            vo.setTaskTitle(task.getTitle());
                        }
                    }

                    vo.setStartTime(record.getStartTime());
                    vo.setEndTime(record.getEndTime());
                    vo.setDuration(record.getDuration());
                    vo.setInterruptions(record.getInterruptions());
                    vo.setFocusScore(record.getFocusScore());
                    vo.setNotes(record.getNotes());
                    vo.setStatus(record.getStatus());
                    vo.setCreatedTime(record.getCreatedTime());
                    return vo;
                })
                .collect(Collectors.toList());
    }

    public FocusRecordVO convertToDetailFocusRecordVO(FocusRecord record, Task task) {
        FocusRecordVO vo = convertToFocusRecordVO(record);
        
        if (task != null) {
            vo.setTask(convertToTaskVO(task));
        }
        
        return vo;
    }
    
    private List<String> parseTags(String tagsStr) {
        if (tagsStr == null || tagsStr.isEmpty() || "null".equals(tagsStr)) {
            return new ArrayList<>();
        }
        
        if (tagsStr.startsWith("[") && tagsStr.endsWith("]")) {
            try {
                List<String> result = objectMapper.readValue(tagsStr, new TypeReference<List<String>>() {});
                result.removeIf(tag -> tag == null || tag.trim().isEmpty());
                return result;
            } catch (Exception e) {
            }
        }
        
        String[] parts = tagsStr.split(",");
        List<String> tags = new ArrayList<>();
        for (String part : parts) {
            String tag = part.trim();
            if (tag.startsWith("\"") && tag.endsWith("\"")) {
                tag = tag.substring(1, tag.length() - 1);
            } else if (tag.startsWith("'") && tag.endsWith("'")) {
                tag = tag.substring(1, tag.length() - 1);
            }
            if (!tag.isEmpty() && !"null".equals(tag)) {
                tags.add(tag);
            }
        }
        return tags;
    }
}