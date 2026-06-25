package com.cjl.self_schedule.common.controller;

import com.cjl.self_schedule.common.exception.BusinessException;
import com.cjl.self_schedule.common.utils.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;

import java.time.LocalDateTime;

public class BaseController {

    /**
     * 从请求中获取用户 ID
     */
    protected Long getUserIdFromRequest(HttpServletRequest request) {
        String token = extractToken(request);
        if (token == null || token.isEmpty()) {
            throw BusinessException.unauthorized("未登录，请先登录");
        }
        return JwtUtil.getUserIdFromToken(token);
    }

    /**
     * 从请求中提取 Token
     */
    protected String extractToken(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        return token;
    }

    /**
     * 验证资源所有权
     */
    protected void validateOwnership(Long resourceUserId, Long userId) {
        if (!resourceUserId.equals(userId)) {
            throw BusinessException.forbidden("无权操作");
        }
    }

    /**
     * 检查资源是否存在
     */
    protected <T> void checkExists(T resource, String resourceName) {
        if (resource == null) {
            throw BusinessException.notFound(resourceName + "不存在");
        }
    }

    /**
     * 验证资源存在性和所有权
     */
    protected <T> void validateResource(T resource, Long resourceUserId, Long userId, String resourceName) {
        checkExists(resource, resourceName);
        validateOwnership(resourceUserId, userId);
    }

    /**
     * 比较两个时间是否相等
     */
    protected boolean equalTime(LocalDateTime time1, LocalDateTime time2) {
        if (time1 == null && time2 == null) {
            return true;
        }
        if (time1 == null || time2 == null) {
            return false;
        }
        return time1.equals(time2);
    }

    /**
     * 比较两个字符串是否相等
     */
    protected boolean equalString(String str1, String str2) {
        if (str1 == null && str2 == null) {
            return true;
        }
        if (str1 == null || str2 == null) {
            return false;
        }
        return str1.equals(str2);
    }

    /**
     * 判断是否为管理员
     */
    protected boolean isAdmin(Integer role) {
        return role != null && role == 1;
    }

    /**
     * 要求管理员权限
     */
    protected void requireAdmin(Integer role) {
        if (!isAdmin(role)) {
            throw BusinessException.forbidden("需要管理员权限");
        }
    }
}