package com.cjl.self_schedule.common.service.impl;

import com.cjl.self_schedule.common.service.CacheService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class CacheServiceImpl implements CacheService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    private static final String PREFIX_CATEGORIES = "task:categories:";
    private static final String PREFIX_TASK_LIST = "task:list:";
    private static final String PREFIX_USER = "user:";
    private static final String PREFIX_AI_PARSE = "ai:parse:";

    private static final long CATEGORIES_EXPIRE = 3600;
    private static final long TASK_LIST_EXPIRE = 1800;
    private static final long USER_EXPIRE = 7200;
    private static final long AI_PARSE_EXPIRE = 600;

    @Override
    public void cacheTaskCategories(Long userId, List<String> categories) {
        String key = PREFIX_CATEGORIES + userId;
        try {
            String json = objectMapper.writeValueAsString(categories);
            redisTemplate.opsForValue().set(key, json, CATEGORIES_EXPIRE, TimeUnit.SECONDS);
        } catch (JsonProcessingException e) {
            log.warn("缓存任务分类序列化失败: {}", e.getMessage());
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<String> getCachedTaskCategories(Long userId) {
        String key = PREFIX_CATEGORIES + userId;
        Object value = redisTemplate.opsForValue().get(key);
        if (value != null) {
            try {
                return objectMapper.readValue(value.toString(), new TypeReference<List<String>>() {});
            } catch (JsonProcessingException e) {
                log.warn("获取缓存任务分类反序列化失败: {}", e.getMessage());
                return null;
            }
        }
        return null;
    }

    @Override
    public void clearTaskCategoriesCache(Long userId) {
        String key = PREFIX_CATEGORIES + userId;
        redisTemplate.delete(key);
    }

    @Override
    public void cacheTaskList(Long userId, Integer pageNum, Integer pageSize, String keySuffix, Object data) {
        String key = PREFIX_TASK_LIST + userId + ":" + pageNum + ":" + pageSize + ":" + (keySuffix != null ? keySuffix : "all");
        try {
            String json = objectMapper.writeValueAsString(data);
            redisTemplate.opsForValue().set(key, json, TASK_LIST_EXPIRE, TimeUnit.SECONDS);
        } catch (JsonProcessingException e) {
            log.warn("缓存任务列表序列化失败: {}", e.getMessage());
        }
    }

    @Override
    public Object getCachedTaskList(Long userId, Integer pageNum, Integer pageSize, String keySuffix) {
        String key = PREFIX_TASK_LIST + userId + ":" + pageNum + ":" + pageSize + ":" + (keySuffix != null ? keySuffix : "all");
        Object value = redisTemplate.opsForValue().get(key);
        if (value != null) {
            try {
                return objectMapper.readValue(value.toString(), Object.class);
            } catch (JsonProcessingException e) {
                log.warn("获取缓存任务列表反序列化失败: {}", e.getMessage());
                return null;
            }
        }
        return null;
    }

    @Override
    public void clearTaskListCache(Long userId) {
        String prefix = PREFIX_TASK_LIST + userId + ":";
        java.util.Set<String> keys = redisTemplate.keys(prefix + "*");
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }

    @Override
    public void cacheUserInfo(Long userId, Object userInfo) {
        String key = PREFIX_USER + userId;
        try {
            String json = objectMapper.writeValueAsString(userInfo);
            redisTemplate.opsForValue().set(key, json, USER_EXPIRE, TimeUnit.SECONDS);
        } catch (JsonProcessingException e) {
            log.warn("缓存用户信息序列化失败: {}", e.getMessage());
        }
    }

    @Override
    public Object getCachedUserInfo(Long userId) {
        String key = PREFIX_USER + userId;
        Object value = redisTemplate.opsForValue().get(key);
        if (value != null) {
            try {
                return objectMapper.readValue(value.toString(), Object.class);
            } catch (JsonProcessingException e) {
                log.warn("获取缓存用户信息反序列化失败: {}", e.getMessage());
                return null;
            }
        }
        return null;
    }

    @Override
    public void clearUserInfoCache(Long userId) {
        String key = PREFIX_USER + userId;
        redisTemplate.delete(key);
    }

    @Override
    public void cacheAiParseResult(String prompt, Object result) {
        String key = PREFIX_AI_PARSE + Math.abs(prompt.hashCode());
        try {
            String json = objectMapper.writeValueAsString(result);
            redisTemplate.opsForValue().set(key, json, AI_PARSE_EXPIRE, TimeUnit.SECONDS);
        } catch (JsonProcessingException e) {
            log.warn("缓存AI解析结果序列化失败: {}", e.getMessage());
        }
    }

    @Override
    public Object getCachedAiParseResult(String prompt) {
        String key = PREFIX_AI_PARSE + Math.abs(prompt.hashCode());
        return redisTemplate.opsForValue().get(key);
    }

    @Override
    public void expire(String key, long seconds) {
        redisTemplate.expire(key, seconds, TimeUnit.SECONDS);
    }
}