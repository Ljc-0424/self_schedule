package com.cjl.self_schedule.task.service;

import com.cjl.self_schedule.task.dto.TaskQueryDTO;
import com.cjl.self_schedule.task.entity.Task;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;

import java.time.LocalDateTime;
import java.util.List;

public interface TaskService extends IService<Task> {

    List<String> getCategories(Long userId);

    List<Task> queryTasks(Long userId, TaskQueryDTO queryDTO);

    IPage<Task> queryTasksPage(Long userId, TaskQueryDTO queryDTO);

    boolean completeTask(Long taskId);

    boolean undoTask(Long taskId);

    List<Task> getAllActiveTasks();

    List<Task> getTasksNeedReminder(LocalDateTime startTime, LocalDateTime endTime);

    List<Task> getCompletedRecurringTasks();

    List<Task> getExpiredRecurringTasks(LocalDateTime thresholdTime);

    LocalDateTime calculateNextRecurrence(Task task);

    Task createNextRecurrence(Task completedTask);

    void deleteNextRecurrence(Task undoneTask);

    void createReminderIndexes();
}