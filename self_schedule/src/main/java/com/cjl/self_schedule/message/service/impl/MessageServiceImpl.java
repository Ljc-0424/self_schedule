package com.cjl.self_schedule.message.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cjl.self_schedule.auth.entity.User;
import com.cjl.self_schedule.auth.service.UserService;
import com.cjl.self_schedule.message.entity.Message;
import com.cjl.self_schedule.message.mapper.MessageMapper;
import com.cjl.self_schedule.message.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class MessageServiceImpl extends ServiceImpl<MessageMapper, Message> implements MessageService {

    @Autowired
    private UserService userService;

    @Override
    public void sendMessage(Long senderId, Long recipientId, String title, String content, Integer messageType) {
        Message message = new Message();
        message.setSenderId(senderId);
        message.setRecipientId(recipientId);
        message.setTitle(title);
        message.setContent(content);
        message.setMessageType(messageType);
        message.setIsRead(0);
        message.setCreatedTime(LocalDateTime.now());
        message.setUpdatedTime(LocalDateTime.now());
        save(message);
    }

    @Override
    public void sendBroadcastMessage(Long senderId, String title, String content) {
        List<User> allUsers = userService.list();
        List<Message> messages = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        for (User user : allUsers) {
            if (user.getRole() != null && user.getRole() == 1) {
                continue;
            }
            Message message = new Message();
            message.setSenderId(senderId);
            message.setRecipientId(user.getId());
            message.setTitle(title);
            message.setContent(content);
            message.setMessageType(1);
            message.setIsRead(0);
            message.setCreatedTime(now);
            message.setUpdatedTime(now);
            messages.add(message);
        }

        saveBatch(messages);
    }

    @Override
    public List<Message> getUserMessages(Long userId) {
        QueryWrapper<Message> query = new QueryWrapper<>();
        query.eq("recipient_id", userId).orderByDesc("created_time");
        return list(query);
    }

    @Override
    public Long getUnreadCount(Long userId) {
        return baseMapper.getUnreadCount(userId);
    }

    @Override
    public void markAsRead(Long messageId, Long userId) {
        Message message = getById(messageId);
        if (message != null && message.getRecipientId().equals(userId)) {
            message.setIsRead(1);
            message.setUpdatedTime(LocalDateTime.now());
            updateById(message);
        }
    }

    @Override
    public void markAllAsRead(Long userId) {
        Message update = new Message();
        update.setIsRead(1);
        update.setUpdatedTime(LocalDateTime.now());
        update(update, new UpdateWrapper<Message>()
                .eq("recipient_id", userId)
                .eq("is_read", 0));
    }
}
