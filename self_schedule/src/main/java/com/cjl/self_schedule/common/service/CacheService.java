package com.cjl.self_schedule.common.service;

import java.util.List;

public interface CacheService {

    List<String> getCachedTaskCategories(Long userId);

    void cacheTaskCategories(Long userId, List<String> categories);

    void clearTaskCategoriesCache(Long userId);

    Object getCachedTaskList(Long userId, Integer pageNum, Integer pageSize, String key);

    void cacheTaskList(Long userId, Integer pageNum, Integer pageSize, String key, Object data);

    void clearTaskListCache(Long userId);

    void cacheUserInfo(Long userId, Object userInfo);

    Object getCachedUserInfo(Long userId);

    void clearUserInfoCache(Long userId);

    void cacheAiParseResult(String prompt, Object result);

    Object getCachedAiParseResult(String prompt);

    void expire(String key, long seconds);
}