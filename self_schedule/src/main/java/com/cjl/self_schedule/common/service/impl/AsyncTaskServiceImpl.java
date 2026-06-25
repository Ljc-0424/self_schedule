package com.cjl.self_schedule.common.service.impl;

import com.cjl.self_schedule.ai.service.AiTaskService;
import com.cjl.self_schedule.ai.service.AiTaskService.AiParseResult;
import com.cjl.self_schedule.common.service.AsyncTaskService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class AsyncTaskServiceImpl implements AsyncTaskService {

    private final StringRedisTemplate redisTemplate;
    private final AiTaskService aiTaskService;
    private final ObjectMapper objectMapper;

    private static final String STATUS_KEY_PREFIX = "async:task:status:";
    private static final String RESULT_KEY_PREFIX = "async:task:result:";
    private static final long TASK_EXPIRE_SECONDS = 300;

    public AsyncTaskServiceImpl(StringRedisTemplate redisTemplate, AiTaskService aiTaskService) {
        this.redisTemplate = redisTemplate;
        this.aiTaskService = aiTaskService;
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public String submitAiTask(Long userId, String prompt) {
        String taskId = UUID.randomUUID().toString();
        
        redisTemplate.opsForValue().set(STATUS_KEY_PREFIX + taskId, TaskStatus.PENDING.name(), TASK_EXPIRE_SECONDS, TimeUnit.SECONDS);
        
        executeAiTask(taskId, userId, prompt);
        
        log.info("提交异步AI任务成功，taskId: {}", taskId);
        return taskId;
    }

    @Async("asyncTaskExecutor")
    public void executeAiTask(String taskId, Long userId, String prompt) {
        try {
            redisTemplate.opsForValue().set(STATUS_KEY_PREFIX + taskId, TaskStatus.PROCESSING.name(), TASK_EXPIRE_SECONDS, TimeUnit.SECONDS);
            
            log.info("开始执行异步AI任务，taskId: {}", taskId);
            
            AiParseResult result = aiTaskService.parseAndCreateTasks(userId, prompt);
            
            String resultJson = objectMapper.writeValueAsString(result);
            redisTemplate.opsForValue().set(RESULT_KEY_PREFIX + taskId, resultJson, TASK_EXPIRE_SECONDS, TimeUnit.SECONDS);
            
            redisTemplate.opsForValue().set(STATUS_KEY_PREFIX + taskId, TaskStatus.COMPLETED.name(), TASK_EXPIRE_SECONDS, TimeUnit.SECONDS);
            
            log.info("异步AI任务执行完成，taskId: {}", taskId);
            
        } catch (Exception e) {
            log.error("异步AI任务执行失败，taskId: {}", taskId, e);
            
            redisTemplate.opsForValue().set(STATUS_KEY_PREFIX + taskId, TaskStatus.FAILED.name(), TASK_EXPIRE_SECONDS, TimeUnit.SECONDS);
            
            AiParseResult errorResult = new AiParseResult("任务处理失败: " + e.getMessage(), false, 0, false);
            try {
                String resultJson = objectMapper.writeValueAsString(errorResult);
                redisTemplate.opsForValue().set(RESULT_KEY_PREFIX + taskId, resultJson, TASK_EXPIRE_SECONDS, TimeUnit.SECONDS);
            } catch (JsonProcessingException ex) {
                log.error("序列化失败结果失败", ex);
            }
        }
    }

    @Override
    public TaskStatus getTaskStatus(String taskId) {
        String status = redisTemplate.opsForValue().get(STATUS_KEY_PREFIX + taskId);
        if (status == null) {
            return null;
        }
        try {
            return TaskStatus.valueOf(status);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    @Override
    public AiParseResult getTaskResult(String taskId) {
        String resultJson = redisTemplate.opsForValue().get(RESULT_KEY_PREFIX + taskId);
        if (resultJson == null) {
            return null;
        }
        try {
            return objectMapper.readValue(resultJson, AiParseResult.class);
        } catch (JsonProcessingException e) {
            log.error("反序列化任务结果失败", e);
            return null;
        }
    }

    @Override
    public void removeTask(String taskId) {
        redisTemplate.delete(STATUS_KEY_PREFIX + taskId);
        redisTemplate.delete(RESULT_KEY_PREFIX + taskId);
        log.info("删除异步任务记录，taskId: {}", taskId);
    }
}