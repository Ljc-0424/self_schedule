package com.cjl.self_schedule.task.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cjl.self_schedule.task.dto.TaskQueryDTO;
import com.cjl.self_schedule.task.entity.Task;
import com.cjl.self_schedule.task.mapper.TaskMapper;
import com.cjl.self_schedule.task.service.TaskService;
import com.cjl.self_schedule.common.utils.QueryWrapperBuilder;
import com.cjl.self_schedule.reminder.service.ReminderService;
import com.cjl.self_schedule.subtask.entity.Subtask;
import com.cjl.self_schedule.subtask.service.SubtaskService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TaskServiceImpl extends ServiceImpl<TaskMapper, Task> implements TaskService {

    private static final Logger logger = LoggerFactory.getLogger(TaskServiceImpl.class);

    @Autowired(required = false)
    private ReminderService reminderService;

    @Autowired(required = false)
    private SubtaskService subtaskService;

    @Override
    public List<String> getCategories(Long userId) {
        QueryWrapper<Task> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId)
                .isNotNull("category")
                .ne("category", "");

        List<Task> tasks = list(queryWrapper);
        return tasks.stream()
                .map(Task::getCategory)
                .filter(c -> c != null && !c.isEmpty())
                .distinct()
                .collect(Collectors.toList());
    }

    @Override
    public List<Task> queryTasks(Long userId, TaskQueryDTO queryDTO) {
        QueryWrapper<Task> queryWrapper = QueryWrapperBuilder.buildTaskQueryWrapper(userId, queryDTO);
        return list(queryWrapper);
    }

    @Override
    public IPage<Task> queryTasksPage(Long userId, TaskQueryDTO queryDTO) {
        QueryWrapper<Task> queryWrapper = QueryWrapperBuilder.buildTaskQueryWrapper(userId, queryDTO);

        int pageNum = queryDTO.getPageNum() != null ? queryDTO.getPageNum() : 1;
        int pageSize = queryDTO.getPageSize() != null ? queryDTO.getPageSize() : 20;

        Page<Task> page = new Page<>(pageNum, pageSize);
        return page(page, queryWrapper);
    }

    @Override
    public boolean completeTask(Long taskId) {
        Task task = getById(taskId);
        if (task == null) {
            return false;
        }

        if (task.getStatus() != null && task.getStatus() == 2) {
            logger.info("任务已完成，跳过重复完成 - taskId: {}", taskId);
            return false;
        }

        task.setStatus(2);
        task.setCompletedTime(LocalDateTime.now());
        task.setUpdatedTime(LocalDateTime.now());

        boolean result = updateById(task);

        if (result && subtaskService != null) {
            List<Subtask> subtasks = subtaskService.findByTaskId(taskId);
            for (Subtask subtask : subtasks) {
                if (subtask.getStatus() != 2) {
                    subtask.setStatus(2);
                    subtask.setCompletedTime(LocalDateTime.now());
                    subtaskService.updateById(subtask);
                }
            }
            logger.info("任务完成，已同步子任务状态 - taskId: {}, subtasksCompleted: {}", taskId, subtasks.size());
        }

        return result;
    }

    @Override
    public boolean undoTask(Long taskId) {
        Task task = getById(taskId);
        if (task == null) {
            return false;
        }

        Integer currentStatus = task.getStatus();
        Integer newStatus = null;

        switch (currentStatus) {
            case 4:
                newStatus = 2;
                if (task.getCompletedTime() == null) {
                    task.setCompletedTime(LocalDateTime.now());
                }
                break;
            case 3:
                newStatus = 0;
                break;
            case 2:
                newStatus = 0;
                task.setCompletedTime(null);
                break;
            default:
                return false;
        }

        task.setStatus(newStatus);
        task.setUpdatedTime(LocalDateTime.now());

        return updateById(task);
    }

    @Override
    public List<Task> getAllActiveTasks() {
        QueryWrapper<Task> queryWrapper = new QueryWrapper<>();
        queryWrapper.in("status", 0, 1)
                .and(qw -> qw.isNotNull("remind_time").or().isNotNull("deadline"));
        return list(queryWrapper);
    }

    @Override
    public boolean save(Task task) {
        return super.save(task);
    }

    @Override
    public boolean updateById(Task task) {
        return super.updateById(task);
    }

    @Override
    public List<Task> getTasksNeedReminder(LocalDateTime startTime, LocalDateTime endTime) {
        return getBaseMapper().findTasksNeedReminder(startTime, endTime);
    }

    @Override
    public List<Task> getCompletedRecurringTasks() {
        return getBaseMapper().findCompletedRecurringTasks();
    }

    @Override
    public List<Task> getExpiredRecurringTasks(LocalDateTime thresholdTime) {
        return getBaseMapper().findExpiredRecurringTasks(thresholdTime);
    }

    @Override
    public LocalDateTime calculateNextRecurrence(Task task) {
        String recurrenceRule = task.getRecurrenceRule();
        if (recurrenceRule == null || recurrenceRule.isEmpty()) {
            return null;
        }

        LocalDateTime baseTime = task.getDeadline() != null ? task.getDeadline() : task.getRemindTime();
        if (baseTime == null) {
            baseTime = task.getCompletedTime() != null ? task.getCompletedTime() : LocalDateTime.now();
        }

        LocalDateTime nextTime = calculateNextRecurrenceFromBase(task, baseTime);

        LocalDateTime now = LocalDateTime.now();
        int maxAttempts = 100;
        while (nextTime != null && !nextTime.isAfter(now) && maxAttempts > 0) {
            nextTime = calculateNextRecurrenceFromBase(task, nextTime);
            maxAttempts--;
        }

        return nextTime;
    }

    private LocalDateTime calculateNextRecurrenceFromRemindTime(Task task) {
        if (task.getRemindTime() == null) {
            return null;
        }
        LocalDateTime nextTime = calculateNextRecurrenceFromBase(task, task.getRemindTime());

        LocalDateTime now = LocalDateTime.now();
        int maxAttempts = 100;
        while (nextTime != null && !nextTime.isAfter(now) && maxAttempts > 0) {
            nextTime = calculateNextRecurrenceFromBase(task, nextTime);
            maxAttempts--;
        }

        return nextTime;
    }

    private LocalDateTime calculateNextRecurrenceFromBase(Task task, LocalDateTime baseTime) {
        String recurrenceRule = task.getRecurrenceRule();
        if (recurrenceRule == null || recurrenceRule.isEmpty()) {
            return null;
        }

        String[] parts = recurrenceRule.split(":");
        String type = parts[0].toUpperCase();
        int interval = 1;
        String params = "";

        if (parts.length >= 2) {
            try {
                interval = Integer.parseInt(parts[1].trim());
            } catch (NumberFormatException e) {
                params = parts[1];
                interval = 1;
            }

            if (parts.length > 2) {
                params = parts[2];
            }
        }

        LocalDateTime nextTime = null;

        switch (type) {
            case "DAILY":
                nextTime = baseTime.plusDays(interval);
                break;
            case "WEEKLY":
                nextTime = calculateWeeklyRecurrence(baseTime, interval, params);
                break;
            case "MONTHLY":
                nextTime = calculateMonthlyRecurrence(baseTime, interval, params);
                break;
            case "YEARLY":
                nextTime = calculateYearlyRecurrence(baseTime, interval, params);
                break;
            default:
                logger.warn("未知的重复规则类型: {}", type);
                return null;
        }

        if (nextTime != null && task.getRecurrenceEndDate() != null) {
            if (nextTime.isAfter(task.getRecurrenceEndDate())) {
                return null;
            }
        }

        return nextTime;
    }

    private LocalDateTime calculateWeeklyRecurrence(LocalDateTime baseTime, int interval, String params) {
        if (params == null || params.isEmpty()) {
            return baseTime.plusWeeks(interval);
        }

        String[] days = params.split(",");
        int currentDayOfWeek = baseTime.getDayOfWeek().getValue();

        for (int i = 1; i <= 7 * interval; i++) {
            int nextDay = (currentDayOfWeek + i - 1) % 7 + 1;
            for (String day : days) {
                if (Integer.parseInt(day.trim()) == nextDay) {
                    return baseTime.plusDays(i);
                }
            }
        }

        return baseTime.plusWeeks(interval);
    }

    private LocalDateTime calculateMonthlyRecurrence(LocalDateTime baseTime, int interval, String params) {
        LocalDateTime nextMonth = baseTime.plusMonths(interval);

        if (params == null || params.isEmpty()) {
            int dayOfMonth = baseTime.getDayOfMonth();
            try {
                return nextMonth.withDayOfMonth(dayOfMonth);
            } catch (Exception e) {
                return nextMonth.withDayOfMonth(nextMonth.getMonth().length(nextMonth.toLocalDate().isLeapYear()));
            }
        }

        if (params.contains("-")) {
            String[] weekDayParts = params.split("-");
            if (weekDayParts.length == 2) {
                try {
                    int weekOfMonth = Integer.parseInt(weekDayParts[0].trim());
                    int dayOfWeek = Integer.parseInt(weekDayParts[1].trim());
                    return calculateNthDayOfMonth(nextMonth, weekOfMonth, dayOfWeek);
                } catch (Exception e) {
                    logger.warn("解析每月重复参数失败: {}", params);
                }
            }
        }

        try {
            int dayOfMonth = Integer.parseInt(params.trim());
            return nextMonth.withDayOfMonth(dayOfMonth);
        } catch (Exception e) {
            return nextMonth.withDayOfMonth(nextMonth.getMonth().length(nextMonth.toLocalDate().isLeapYear()));
        }
    }

    private LocalDateTime calculateNthDayOfMonth(LocalDateTime date, int weekOfMonth, int dayOfWeek) {
        LocalDateTime firstDay = date.withDayOfMonth(1);
        int firstDayOfWeek = firstDay.getDayOfWeek().getValue();

        int daysToAdd = (dayOfWeek - firstDayOfWeek + 7) % 7;
        LocalDateTime firstTargetDay = firstDay.plusDays(daysToAdd);

        if (weekOfMonth < 0) {
            LocalDateTime lastDay = date.withDayOfMonth(date.getMonth().length(date.toLocalDate().isLeapYear()));
            int lastDayOfWeek = lastDay.getDayOfWeek().getValue();
            int daysToSubtract = (lastDayOfWeek - dayOfWeek + 7) % 7;
            LocalDateTime lastTargetDay = lastDay.minusDays(daysToSubtract);
            return lastTargetDay.plusWeeks(weekOfMonth + 1);
        }

        return firstTargetDay.plusWeeks(weekOfMonth - 1);
    }

    private LocalDateTime calculateYearlyRecurrence(LocalDateTime baseTime, int interval, String params) {
        LocalDateTime nextYear = baseTime.plusYears(interval);

        if (params == null || params.isEmpty()) {
            return nextYear.withMonth(baseTime.getMonthValue()).withDayOfMonth(baseTime.getDayOfMonth());
        }

        String[] dateParts = params.split("-");
        if (dateParts.length == 2) {
            try {
                int month = Integer.parseInt(dateParts[0].trim());
                int day = Integer.parseInt(dateParts[1].trim());
                return nextYear.withMonth(month).withDayOfMonth(day);
            } catch (Exception e) {
                logger.warn("解析每年重复参数失败: {}", params);
            }
        }

        return nextYear.withMonth(baseTime.getMonthValue()).withDayOfMonth(baseTime.getDayOfMonth());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Task createNextRecurrence(Task completedTask) {
        LocalDateTime nextTime = calculateNextRecurrence(completedTask);
        if (nextTime == null) {
            return null;
        }

        LocalDateTime newDeadline = null;
        LocalDateTime newRemindTime = null;

        if (completedTask.getDeadline() != null && completedTask.getRemindTime() != null) {
            long offsetSeconds = java.time.Duration.between(completedTask.getRemindTime(), completedTask.getDeadline()).getSeconds();
            LocalDateTime baseForRemind = calculateNextRecurrenceFromRemindTime(completedTask);
            if (baseForRemind != null) {
                newRemindTime = baseForRemind;
                newDeadline = baseForRemind.plusSeconds(offsetSeconds);
            } else {
                newRemindTime = nextTime;
                newDeadline = nextTime.plusSeconds(offsetSeconds);
            }
        } else if (completedTask.getDeadline() != null) {
            newDeadline = nextTime;
        } else if (completedTask.getRemindTime() != null) {
            newRemindTime = nextTime;
        }

        QueryWrapper<Task> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", completedTask.getUserId())
                   .eq("title", completedTask.getTitle())
                   .in("status", 0, 1);

        if (newDeadline != null) {
            queryWrapper.eq("deadline", newDeadline);
        } else if (newRemindTime != null) {
            queryWrapper.eq("remind_time", newRemindTime);
        }

        long existingCount = count(queryWrapper);

        if (existingCount > 0) {
            logger.info("已存在相同的重复任务，跳过创建 - userId: {}, title: {}, time: {}",
                    completedTask.getUserId(), completedTask.getTitle(), nextTime);
            return null;
        }

        Task newTask = new Task();
        newTask.setUserId(completedTask.getUserId());
        newTask.setTitle(completedTask.getTitle());
        newTask.setDescription(completedTask.getDescription());
        newTask.setPriority(completedTask.getPriority());
        newTask.setStatus(0);
        newTask.setDeadline(newDeadline);
        newTask.setRemindTime(newRemindTime);
        newTask.setEstimatedSeconds(completedTask.getEstimatedSeconds());
        newTask.setActualSeconds(0);
        newTask.setCategory(completedTask.getCategory());
        newTask.setTags(completedTask.getTags());
        newTask.setReminderConfig(completedTask.getReminderConfig());
        newTask.setIsAiGenerated(completedTask.getIsAiGenerated());
        newTask.setRecurrenceRule(completedTask.getRecurrenceRule());
        newTask.setRecurrenceEndDate(completedTask.getRecurrenceEndDate());
        newTask.setCreatedTime(LocalDateTime.now());
        newTask.setUpdatedTime(LocalDateTime.now());

        if (save(newTask)) {
            if (reminderService != null && (newTask.getRemindTime() != null || newTask.getDeadline() != null)) {
                try {
                    reminderService.updateTaskReminders(newTask);
                } catch (Exception e) {
                    logger.error("重复任务创建后同步提醒记录失败: {}", e.getMessage());
                }
            }
            logger.info("已为重复任务创建下一次任务 - originalTaskId: {}, newTaskId: {}, nextTime: {}",
                    completedTask.getId(), newTask.getId(), nextTime);
            return newTask;
        }

        return null;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteNextRecurrence(Task undoneTask) {
        LocalDateTime nextTime = calculateNextRecurrence(undoneTask);
        if (nextTime == null) {
            return;
        }

        QueryWrapper<Task> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", undoneTask.getUserId())
                   .eq("title", undoneTask.getTitle())
                   .in("status", 0, 1);

        if (undoneTask.getDeadline() != null) {
            queryWrapper.eq("deadline", nextTime);
        } else if (undoneTask.getRemindTime() != null) {
            queryWrapper.eq("remind_time", nextTime);
        } else {
            return;
        }

        List<Task> nextTasks = getBaseMapper().selectList(queryWrapper);
        for (Task nextTask : nextTasks) {
            if (reminderService != null) {
                reminderService.deleteTaskReminders(nextTask.getId());
            }
            removeById(nextTask.getId());
            logger.info("撤销重复任务完成，已删除自动创建的下一跳任务及其提醒记录 - undoneTaskId: {}, deletedTaskId: {}, time: {}",
                    undoneTask.getId(), nextTask.getId(), nextTime);
        }
    }

    @Override
    public void createReminderIndexes() {
        getBaseMapper().createIndexOnStatusAndRemindTime();
        getBaseMapper().createIndexOnStatusAndDeadline();
        getBaseMapper().createIndexOnUserIdAndStatus();
    }
}