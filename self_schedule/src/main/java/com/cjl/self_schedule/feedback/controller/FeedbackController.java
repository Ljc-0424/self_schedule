package com.cjl.self_schedule.feedback.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cjl.self_schedule.auth.entity.User;
import com.cjl.self_schedule.auth.service.UserService;
import com.cjl.self_schedule.common.controller.BaseController;
import com.cjl.self_schedule.common.vo.Result;
import com.cjl.self_schedule.feedback.dto.FeedbackCreateDTO;
import com.cjl.self_schedule.feedback.entity.Feedback;
import com.cjl.self_schedule.feedback.service.FeedbackService;
import com.cjl.self_schedule.feedback.vo.FeedbackVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/feedback")
@Api(tags = "用户反馈")
public class FeedbackController extends BaseController {

    @Autowired
    private FeedbackService feedbackService;

    @Autowired
    private UserService userService;

    @PostMapping
    @ApiOperation(value = "提交反馈")
    public Result<Void> createFeedback(@Valid @RequestBody FeedbackCreateDTO dto, HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);

        Feedback feedback = new Feedback();
        feedback.setUserId(userId);
        feedback.setCategory(dto.getCategory() != null ? dto.getCategory() : "other");
        feedback.setTitle(dto.getTitle().trim());
        feedback.setContent(dto.getContent().trim());
        feedback.setContact(dto.getContact());
        feedback.setStatus(0);

        feedbackService.save(feedback);
        return Result.success("反馈提交成功", null);
    }

    @GetMapping("/my")
    @ApiOperation(value = "查看我的反馈")
    public Result<List<FeedbackVO>> getMyFeedbacks(HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);

        QueryWrapper<Feedback> query = new QueryWrapper<>();
        query.eq("user_id", userId).orderByDesc("created_time");

        List<Feedback> list = feedbackService.list(query);

        // 批量查询用户信息
        Set<Long> userIds = list.stream().map(Feedback::getUserId).collect(Collectors.toSet());
        Map<Long, User> userMap = batchGetUsers(new ArrayList<>(userIds));

        List<FeedbackVO> voList = list.stream()
                .map(f -> convertVO(f, userMap))
                .collect(Collectors.toList());

        return Result.success(voList);
    }

    @GetMapping("/unread-count")
    @ApiOperation(value = "获取未读反馈回复数量")
    public Result<Map<String, Long>> getUnreadCount(HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);

        long count = feedbackService.count(new QueryWrapper<Feedback>()
                .eq("user_id", userId)
                .eq("user_read", 0)
                .isNotNull("admin_reply"));

        Map<String, Long> data = new HashMap<>();
        data.put("count", count);
        return Result.success(data);
    }

    @PutMapping("/mark-read")
    @ApiOperation(value = "标记所有反馈回复为已读")
    public Result<Void> markAllAsRead(HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);

        Feedback update = new Feedback();
        update.setUserRead(1);

        feedbackService.update(update, new QueryWrapper<Feedback>()
                .eq("user_id", userId)
                .eq("user_read", 0)
                .isNotNull("admin_reply"));

        return Result.success(null);
    }

    /**
     * 批量查询用户，返回 userId -> User 映射
     */
    private Map<Long, User> batchGetUsers(List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Collections.emptyMap();
        }
        List<User> users = userService.listByIds(userIds);
        return users.stream().collect(Collectors.toMap(User::getId, u -> u, (a, b) -> a));
    }

    /**
     * 使用预加载的用户映射转换 FeedbackVO
     */
    private FeedbackVO convertVO(Feedback feedback, Map<Long, User> userMap) {
        FeedbackVO vo = new FeedbackVO();
        vo.setId(feedback.getId());
        vo.setUserId(feedback.getUserId());
        vo.setCategory(feedback.getCategory());
        vo.setTitle(feedback.getTitle());
        vo.setContent(feedback.getContent());
        vo.setContact(feedback.getContact());
        vo.setStatus(feedback.getStatus());
        vo.setAdminReply(feedback.getAdminReply());
        vo.setUserRead(feedback.getUserRead());
        vo.setCreatedTime(feedback.getCreatedTime());
        vo.setUpdatedTime(feedback.getUpdatedTime());

        User user = userMap.get(feedback.getUserId());
        if (user != null) {
            vo.setUsername(user.getUsername());
            vo.setNickname(user.getNickname());
        }
        return vo;
    }
}
