package com.cjl.self_schedule.common.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.util.ContentCachingRequestWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;

@Slf4j
@Component
@Order(1)
public class RequestLoggingFilter implements Filter {

    private static final java.util.Set<String> SENSITIVE_PATHS = java.util.Set.of(
            "/api/auth/login",
            "/api/auth/register",
            "/api/auth/change-password",
            "/api/auth/reset-password",
            "/api/user/info"
    );

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) 
            throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        
        ContentCachingRequestWrapper wrappedRequest = new ContentCachingRequestWrapper(httpRequest, 1024 * 1024);
        
        String method = wrappedRequest.getMethod();
        String requestUri = wrappedRequest.getRequestURI();
        String queryString = wrappedRequest.getQueryString();
        String remoteAddr = wrappedRequest.getRemoteAddr();
        String remoteHost = wrappedRequest.getRemoteHost();
        int remotePort = wrappedRequest.getRemotePort();
        
        String authorizationHeader = wrappedRequest.getHeader("Authorization");
        
        log.debug("========== 请求开始 ==========");
        log.debug("请求方法: {}", method);
        log.debug("请求路径: {}", requestUri);
        log.debug("客户端IP: {} ({}:{})", remoteAddr, remoteHost, remotePort);
        if (queryString != null) {
            log.debug("查询参数: {}", queryString);
        }
        
        log.debug("Authorization Header: {}", authorizationHeader != null ? 
            (authorizationHeader.startsWith("Bearer ") ? "Bearer [token]" : "存在但非Bearer格式") : "为空");
        
        log.debug("所有请求头:");
        Enumeration<String> headerNames = wrappedRequest.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            String headerValue = headerName.equalsIgnoreCase("Authorization") ? 
                (authorizationHeader.startsWith("Bearer ") ? "Bearer [token]" : authorizationHeader) : 
                wrappedRequest.getHeader(headerName);
            log.debug("  {}: {}", headerName, headerValue);
        }
        
        int contentLength = wrappedRequest.getContentLength();
        log.debug("Content-Length: {}", contentLength);
        
        try {
            chain.doFilter(wrappedRequest, response);
        } catch (Exception e) {
            log.error("请求处理异常: {}", e.getMessage(), e);
            throw e;
        }
        
        if ("POST".equalsIgnoreCase(method) || "PUT".equalsIgnoreCase(method)) {
            String contentType = wrappedRequest.getContentType();
            log.info("Content-Type: {}", contentType);
            if (contentType != null && contentType.contains(MediaType.APPLICATION_JSON_VALUE)) {
                if (SENSITIVE_PATHS.contains(requestUri)) {
                    log.info("请求体: [敏感接口，已脱敏]");
                } else {
                    byte[] bodyBytes = wrappedRequest.getContentAsByteArray();
                    if (bodyBytes != null && bodyBytes.length > 0) {
                        String body = new String(bodyBytes, StandardCharsets.UTF_8);
                        log.info("请求体: {}", body);
                    } else {
                        log.info("请求体: 空或无法读取");
                    }
                }
            }
        }
        
        log.info("响应状态码: {}", httpResponse.getStatus());
        log.info("========== 请求结束 ==========");
    }
}
