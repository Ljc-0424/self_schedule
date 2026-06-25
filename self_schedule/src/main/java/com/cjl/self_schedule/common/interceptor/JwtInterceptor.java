package com.cjl.self_schedule.common.interceptor;

import com.cjl.self_schedule.common.utils.JwtUtil;
import com.cjl.self_schedule.common.vo.Result;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.HandlerInterceptor;

@Slf4j
public class JwtInterceptor implements HandlerInterceptor {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    private void writeJsonResponse(HttpServletResponse response, int httpStatus, Result<?> result) {
        try {
            response.setStatus(httpStatus);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write(objectMapper.writeValueAsString(result));
        } catch (Exception e) {
            log.error("写入JSON响应失败", e);
        }
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String authorizationHeader = request.getHeader("Authorization");

        // 安全修复：移除通用的URL query token回退（防止token泄露到日志/Referer）
        // 仅对SSE端点保留query token支持（SSE无法自定义Header）
        if (authorizationHeader == null || authorizationHeader.isEmpty()) {
            String requestUri = request.getRequestURI();
            if (requestUri.contains("/api/sse/connect")) {
                String queryToken = request.getParameter("token");
                if (queryToken != null && !queryToken.isEmpty()) {
                    authorizationHeader = "Bearer " + queryToken;
                }
            }
        }

        if (authorizationHeader == null || authorizationHeader.isEmpty()) {
            writeJsonResponse(response, HttpServletResponse.SC_UNAUTHORIZED, Result.error(401, "Missing authorization token"));
            return false;
        }

        String token = authorizationHeader;
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        try {
            JwtUtil.validateToken(token);
            return true;
        } catch (Exception e) {
            log.warn("Token验证失败: {}", e.getMessage());
            writeJsonResponse(response, HttpServletResponse.SC_UNAUTHORIZED, Result.error(401, "Invalid or expired token"));
            return false;
        }
    }
}