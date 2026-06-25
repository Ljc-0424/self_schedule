package com.cjl.self_schedule.common.service;

public interface DataCleanupService {
    void cleanupExpiredTasks();
    void cleanupExpiredFeedbacks();
    void cleanupExpiredFocusRecords();
    void cleanupExpiredMessages();
    void cleanupExpiredAppeals();
}
