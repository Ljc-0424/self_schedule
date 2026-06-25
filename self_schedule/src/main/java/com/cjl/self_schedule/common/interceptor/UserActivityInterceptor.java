package com.cjl.self_schedule.common.interceptor;

import com.cjl.self_schedule.common.utils.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class UserActivityInterceptor implements HandlerInterceptor {

    private static final String USER_ACTIVE_PREFIX = "user:active:";

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String authorizationHeader = request.getHeader("Authorization");
        if (authorizationHeader == null || authorizationHeader.isEmpty()) {
            return true;
        }

        String token = authorizationHeader.startsWith("Bearer ") ? authorizationHeader.substring(7) : authorizationHeader;

        try {
            Long userId = JwtUtil.getUserIdFromToken(token);
            String key = USER_ACTIVE_PREFIX + userId;
            redisTemplate.opsForValue().set(key, String.valueOf(System.currentTimeMillis()), 20, TimeUnit.MINUTES);
        } catch (Exception ignored) {
        }

        return true;
    }
}
