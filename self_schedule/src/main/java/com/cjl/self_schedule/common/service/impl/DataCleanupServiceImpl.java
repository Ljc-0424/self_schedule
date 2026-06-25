package com.cjl.self_schedule.common.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cjl.self_schedule.appeal.mapper.AppealMapper;
import com.cjl.self_schedule.feedback.mapper.FeedbackMapper;
import com.cjl.self_schedule.focus.mapper.FocusRecordMapper;
import com.cjl.self_schedule.message.mapper.MessageMapper;
import com.cjl.self_schedule.reminder.service.ReminderService;
import com.cjl.self_schedule.subtask.service.SubtaskService;
import com.cjl.self_schedule.task.entity.Task;
import com.cjl.self_schedule.task.mapper.TaskMapper;
import com.cjl.self_schedule.task.service.TaskService;
import com.cjl.self_schedule.common.service.DataCleanupService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class DataCleanupServiceImpl implements DataCleanupService {

    @Autowired
    private TaskMapper taskMapper;

    @Autowired
    private TaskService taskService;

    @Autowired
    private SubtaskService subtaskService;

    @Autowired(required = false)
    private ReminderService reminderService;

    @Autowired
    private FeedbackMapper feedbackMapper;

    @Autowired
    private FocusRecordMapper focusRecordMapper;

    @Autowired
    private MessageMapper messageMapper;

    @Autowired
    private AppealMapper appealMapper;

    @Value("${app.cleanup.enabled:true}")
    private boolean cleanupEnabled;

    @Value("${app.cleanup.archived-task-days:30}")
    private int archivedTaskDays;

    @Value("${app.cleanup.cancelled-task-days:15}")
    private int cancelledTaskDays;

    @Value("${app.cleanup.closed-feedback-days:60}")
    private int closedFeedbackDays;

    @Value("${app.cleanup.read-feedback-days:90}")
    private int readFeedbackDays;

    @Value("${app.cleanup.focus-record-days:90}")
    private int focusRecordDays;

    @Value("${app.cleanup.read-message-days:90}")
    private int readMessageDays;

    @Value("${app.cleanup.closed-appeal-days:60}")
    private int closedAppealDays;

    @Override
    @Scheduled(cron = "0 30 2 * * ?")
    public void cleanupExpiredTasks() {
        if (!cleanupEnabled) {
            return;
        }
        log.info("开始清理过期任务...");

        LocalDateTime archivedBefore = LocalDateTime.now().minusDays(archivedTaskDays);
        List<Task> archivedTasks = taskService.list(new QueryWrapper<Task>()
                .eq("status", 4)
                .le("updated_time", archivedBefore));

        int archivedDeleted = 0;
        for (Task task : archivedTasks) {
            if (reminderService != null) {
                reminderService.deleteTaskReminders(task.getId());
            }
            subtaskService.deleteByTaskId(task.getId());
            taskMapper.deleteById(task.getId());
            archivedDeleted++;
        }
        if (archivedDeleted > 0) {
            log.info("清理已存档任务 {} 条（超过 {} 天）", archivedDeleted, archivedTaskDays);
        }

        LocalDateTime cancelledBefore = LocalDateTime.now().minusDays(cancelledTaskDays);
        List<Task> cancelledTasks = taskService.list(new QueryWrapper<Task>()
                .eq("status", 3)
                .le("updated_time", cancelledBefore));

        int cancelledDeleted = 0;
        for (Task task : cancelledTasks) {
            if (reminderService != null) {
                reminderService.deleteTaskReminders(task.getId());
            }
            subtaskService.deleteByTaskId(task.getId());
            taskMapper.deleteById(task.getId());
            cancelledDeleted++;
        }
        if (cancelledDeleted > 0) {
            log.info("清理已取消任务 {} 条（超过 {} 天）", cancelledDeleted, cancelledTaskDays);
        }

        log.info("任务清理完成，共清理 {} 条", archivedDeleted + cancelledDeleted);
    }

    @Override
    @Scheduled(cron = "0 0 3 * * ?")
    public void cleanupExpiredFeedbacks() {
        if (!cleanupEnabled) {
            return;
        }
        log.info("开始清理过期反馈...");

        LocalDateTime closedBefore = LocalDateTime.now().minusDays(closedFeedbackDays);
        int closedDeleted = feedbackMapper.deleteByStatusAndTime(2, closedBefore);
        if (closedDeleted > 0) {
            log.info("清理已关闭反馈 {} 条（超过 {} 天）", closedDeleted, closedFeedbackDays);
        }

        LocalDateTime readBefore = LocalDateTime.now().minusDays(readFeedbackDays);
        int readDeleted = feedbackMapper.deleteProcessedAndRead(1, readBefore);
        if (readDeleted > 0) {
            log.info("清理已处理且已读反馈 {} 条（超过 {} 天）", readDeleted, readFeedbackDays);
        }

        log.info("反馈清理完成，共清理 {} 条", closedDeleted + readDeleted);
    }

    @Override
    @Scheduled(cron = "0 30 3 * * ?")
    public void cleanupExpiredFocusRecords() {
        if (!cleanupEnabled) {
            return;
        }
        log.info("开始清理过期专注记录...");

        LocalDateTime before = LocalDateTime.now().minusDays(focusRecordDays);
        int deleted = focusRecordMapper.deleteByCreatedTime(before);
        if (deleted > 0) {
            log.info("清理专注记录 {} 条（超过 {} 天）", deleted, focusRecordDays);
        }

        log.info("专注记录清理完成，共清理 {} 条", deleted);
    }

    @Override
    @Scheduled(cron = "0 0 4 * * ?")
    public void cleanupExpiredMessages() {
        if (!cleanupEnabled) {
            return;
        }
        log.info("开始清理过期消息...");

        LocalDateTime readBefore = LocalDateTime.now().minusDays(readMessageDays);
        int deleted = messageMapper.delete(new QueryWrapper<com.cjl.self_schedule.message.entity.Message>()
                .eq("is_read", 1)
                .le("updated_time", readBefore));
        if (deleted > 0) {
            log.info("清理已读消息 {} 条（超过 {} 天）", deleted, readMessageDays);
        }

        log.info("消息清理完成，共清理 {} 条", deleted);
    }

    @Override
    @Scheduled(cron = "0 30 4 * * ?")
    public void cleanupExpiredAppeals() {
        if (!cleanupEnabled) {
            return;
        }
        log.info("开始清理过期申诉...");

        LocalDateTime closedBefore = LocalDateTime.now().minusDays(closedAppealDays);
        int deleted = appealMapper.delete(new QueryWrapper<com.cjl.self_schedule.appeal.entity.Appeal>()
                .in("status", 2, 3)
                .le("updated_time", closedBefore));
        if (deleted > 0) {
            log.info("清理已关闭申诉 {} 条（超过 {} 天）", deleted, closedAppealDays);
        }

        log.info("申诉清理完成，共清理 {} 条", deleted);
    }

}
