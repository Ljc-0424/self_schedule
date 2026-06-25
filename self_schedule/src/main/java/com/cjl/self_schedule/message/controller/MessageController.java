package com.cjl.self_schedule.message.controller;

import com.cjl.self_schedule.common.controller.BaseController;
import com.cjl.self_schedule.common.vo.Result;
import com.cjl.self_schedule.message.entity.Message;
import com.cjl.self_schedule.message.service.MessageService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/messages")
@Api(tags = "用户消息")
public class MessageController extends BaseController {

    @Autowired
    private MessageService messageService;

    @GetMapping
    @ApiOperation(value = "获取我的消息列表")
    public Result<List<Message>> getMyMessages(HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        List<Message> messages = messageService.getUserMessages(userId);
        return Result.success(messages);
    }

    @GetMapping("/unread-count")
    @ApiOperation(value = "获取未读消息数量")
    public Result<Map<String, Long>> getUnreadCount(HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        Long count = messageService.getUnreadCount(userId);
        Map<String, Long> data = new HashMap<>();
        data.put("count", count);
        return Result.success(data);
    }

    @PutMapping("/{id}/read")
    @ApiOperation(value = "标记消息为已读")
    public Result<Void> markAsRead(@PathVariable Long id, HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        messageService.markAsRead(id, userId);
        return Result.success(null);
    }

    @PutMapping("/mark-all-read")
    @ApiOperation(value = "标记所有消息为已读")
    public Result<Void> markAllAsRead(HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        messageService.markAllAsRead(userId);
        return Result.success(null);
    }
}
