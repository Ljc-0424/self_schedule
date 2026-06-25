package com.cjl.self_schedule.ai.service;

import com.cjl.self_schedule.task.dto.TaskCreateDTO;
import com.cjl.self_schedule.auth.entity.User;

import java.util.List;

public interface AiTaskService {

    List<TaskCreateDTO> parseTaskFromPrompt(String prompt);

    List<TaskCreateDTO> parseTaskFromPrompt(String prompt, User user);

    AiParseResult parseAndCreateTasks(Long userId, String prompt);

    record AiParseResult(
            String message,
            boolean taskCreated,
            int taskCount,
            boolean isAiGenerated
    ) {}
}