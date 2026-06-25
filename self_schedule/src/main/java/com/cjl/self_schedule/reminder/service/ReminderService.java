package com.cjl.self_schedule.reminder.service;

import com.cjl.self_schedule.reminder.entity.ReminderRecord;
import com.cjl.self_schedule.common.vo.PageResult;
import com.cjl.self_schedule.task.entity.Task;

import java.time.LocalDateTime;
import java.util.List;

public interface ReminderService {

    void scanAndTriggerReminders();

    void createReminder(Long taskId, Long userId, String taskTitle,
                        LocalDateTime taskTime, LocalDateTime triggerTime, String reminderType);

    List<ReminderRecord> getPendingReminders(Long userId);

    PageResult<ReminderRecord> getAllReminders(Long userId, Integer page, Integer pageSize);

    PageResult<ReminderRecord> getRemindersByStatus(Long userId, String status, Integer page, Integer pageSize);

    void updateReminderStatus(Long id, String status);

    void markAllAsRead(Long userId);

    void deleteReminder(Long id);

    void deleteTaskReminders(Long taskId);

    void updateTaskReminders(Task task);

    void markAsExpired(Long userId);

    int deleteByStatus(Long userId, List<String> status);

    ReminderStats getReminderStats(Long userId);

    class ReminderStats {
        private final int total;
        private final int pending;
        private final int sent;
        private final int read;
        private final int dismissed;
        private final int expired;

        public ReminderStats(int total, int pending, int sent, int read, int dismissed, int expired) {
            this.total = total;
            this.pending = pending;
            this.sent = sent;
            this.read = read;
            this.dismissed = dismissed;
            this.expired = expired;
        }

        public int getTotal() { return total; }
        public int getPending() { return pending; }
        public int getSent() { return sent; }
        public int getRead() { return read; }
        public int getDismissed() { return dismissed; }
        public int getExpired() { return expired; }
    }
}