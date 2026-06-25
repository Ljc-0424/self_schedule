package com.cjl.self_schedule.message.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cjl.self_schedule.message.entity.Message;

import java.util.List;

public interface MessageService extends IService<Message> {

    void sendMessage(Long senderId, Long recipientId, String title, String content, Integer messageType);

    void sendBroadcastMessage(Long senderId, String title, String content);

    List<Message> getUserMessages(Long userId);

    Long getUnreadCount(Long userId);

    void markAsRead(Long messageId, Long userId);

    void markAllAsRead(Long userId);
}
