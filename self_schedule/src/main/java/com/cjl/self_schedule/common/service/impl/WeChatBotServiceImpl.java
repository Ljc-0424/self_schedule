package com.cjl.self_schedule.common.service.impl;

import com.cjl.self_schedule.common.service.WeChatBotService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Service
public class WeChatBotServiceImpl implements WeChatBotService {

    private static final Logger logger = LoggerFactory.getLogger(WeChatBotServiceImpl.class);
    
    private static final int MAX_RETRY = 2;
    private static final long RETRY_DELAY_MS = 1000;

    @Value("${wechat.bot.webhook-url:}")
    private String webhookUrl;

    private final RestTemplate restTemplate;

    public WeChatBotServiceImpl(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public void sendTextMessage(String content) {
        if (!isConfigured()) {
            logger.warn("企业微信机器人未配置，跳过发送消息");
            return;
        }
        
        sendTextMessageWithRetry(content, webhookUrl, 0);
    }

    @Override
    public void sendTaskReminder(String taskTitle, String taskTime, String reminderType) {
        String content = buildReminderContent(taskTitle, taskTime, reminderType);
        sendTextMessage(content);
    }

    @Override
    public void sendTextMessage(String content, String userWebhookUrl) {
        String targetUrl = (userWebhookUrl != null && !userWebhookUrl.isEmpty()) ? userWebhookUrl : webhookUrl;
        
        if (!isUrlConfigured(targetUrl)) {
            logger.warn("企业微信机器人未配置，跳过发送消息");
            return;
        }
        
        sendTextMessageWithRetry(content, targetUrl, 0);
    }

    @Override
    public void sendTaskReminder(String taskTitle, String taskTime, String reminderType, String userWebhookUrl) {
        String content = buildReminderContent(taskTitle, taskTime, reminderType);
        sendTextMessage(content, userWebhookUrl);
    }
    
    private void sendTextMessageWithRetry(String content, String targetUrl, int retryCount) {
        try {
            Map<String, Object> body = new HashMap<>();
            body.put("msgtype", "text");
            
            Map<String, String> text = new HashMap<>();
            text.put("content", content);
            body.put("text", text);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
            
            ResponseEntity<String> response = restTemplate.postForEntity(targetUrl, entity, String.class);
            
            if (response.getStatusCode().is2xxSuccessful()) {
                logger.info("企业微信消息发送成功（重试次数: {}）", retryCount);
            } else {
                logger.error("企业微信消息发送失败，HTTP状态码: {}, 响应: {}", 
                        response.getStatusCode(), response.getBody());
                
                if (response.getStatusCode() == HttpStatus.TOO_MANY_REQUESTS && retryCount < MAX_RETRY) {
                    logger.warn("请求被限流，进行第 {} 次重试", retryCount + 1);
                    Thread.sleep(RETRY_DELAY_MS * (retryCount + 1));
                    sendTextMessageWithRetry(content, targetUrl, retryCount + 1);
                }
            }
        } catch (ResourceAccessException e) {
            logger.warn("发送企业微信消息网络异常（重试次数: {}）: {}", retryCount, e.getMessage());
            
            if (retryCount < MAX_RETRY) {
                try {
                    Thread.sleep(RETRY_DELAY_MS * (retryCount + 1));
                    sendTextMessageWithRetry(content, targetUrl, retryCount + 1);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    logger.error("重试等待被中断", ie);
                }
            } else {
                logger.error("发送企业微信消息失败，已达最大重试次数: {}", e.getMessage());
                throw new RuntimeException("发送企业微信消息失败: " + e.getMessage(), e);
            }
        } catch (RestClientException e) {
            logger.error("发送企业微信消息客户端错误: {}", e.getMessage(), e);
            throw new RuntimeException("发送企业微信消息失败: " + e.getMessage(), e);
        } catch (Exception e) {
            logger.error("发送企业微信消息异常: {}", e.getMessage(), e);
            throw new RuntimeException("发送企业微信消息失败: " + e.getMessage(), e);
        }
    }

    private String buildReminderContent(String taskTitle, String taskTime, String reminderType) {
        String timeStr = formatTime(taskTime);
        if ("DEADLINE".equals(reminderType)) {
            return String.format("📅 %s\n截止：%s", taskTitle, timeStr);
        } else {
            return String.format("⏰ %s\n时间：%s", taskTitle, timeStr);
        }
    }
    
    private String formatTime(String taskTime) {
        if (taskTime == null || taskTime.isEmpty()) {
            return "";
        }
        try {
            String cleanTime = taskTime.replace("T", " ").replace(":00$", "");
            java.time.LocalDateTime now = java.time.LocalDateTime.now();
            java.time.LocalDateTime taskDateTime = java.time.LocalDateTime.parse(taskTime.replace(" ", "T"));
            java.time.LocalDate today = now.toLocalDate();
            java.time.LocalDate taskDate = taskDateTime.toLocalDate();
            
            if (taskDate.equals(today)) {
                return taskDateTime.toLocalTime().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm"));
            } else if (taskDate.equals(today.plusDays(1))) {
                return "明天 " + taskDateTime.toLocalTime().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm"));
            } else {
                return cleanTime;
            }
        } catch (Exception e) {
            return taskTime.replace("T", " ");
        }
    }

    private boolean isConfigured() {
        return webhookUrl != null && !webhookUrl.isEmpty();
    }

    private boolean isUrlConfigured(String url) {
        return url != null && !url.isEmpty();
    }
}