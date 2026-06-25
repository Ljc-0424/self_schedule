package com.cjl.self_schedule.common.service;

import org.springframework.stereotype.Service;

/**
 * 企业微信机器人服务接口
 */
public interface WeChatBotService {

    /**
     * 发送文本消息（使用全局配置的webhook）
     * @param content 消息内容
     */
    void sendTextMessage(String content);

    /**
     * 发送任务提醒消息（使用全局配置的webhook）
     * @param taskTitle 任务标题
     * @param taskTime 任务时间
     * @param reminderType 提醒类型
     */
    void sendTaskReminder(String taskTitle, String taskTime, String reminderType);

    /**
     * 发送文本消息（使用用户自定义的webhook）
     * @param content 消息内容
     * @param userWebhookUrl 用户自定义的webhook地址
     */
    void sendTextMessage(String content, String userWebhookUrl);

    /**
     * 发送任务提醒消息（使用用户自定义的webhook）
     * @param taskTitle 任务标题
     * @param taskTime 任务时间
     * @param reminderType 提醒类型
     * @param userWebhookUrl 用户自定义的webhook地址
     */
    void sendTaskReminder(String taskTitle, String taskTime, String reminderType, String userWebhookUrl);
}