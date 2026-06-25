package com.cjl.self_schedule.common.service;

import com.cjl.self_schedule.ai.service.AiTaskService.AiParseResult;

public interface AsyncTaskService {

    String submitAiTask(Long userId, String prompt);

    TaskStatus getTaskStatus(String taskId);

    AiParseResult getTaskResult(String taskId);

    void removeTask(String taskId);

    enum TaskStatus {
        PENDING("待处理"),
        PROCESSING("处理中"),
        COMPLETED("已完成"),
        FAILED("失败");

        private final String description;

        TaskStatus(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }
}