package com.cjl.self_schedule.admin.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cjl.self_schedule.appeal.dto.AppealAuditDTO;
import com.cjl.self_schedule.appeal.dto.BanUserDTO;
import com.cjl.self_schedule.appeal.entity.Appeal;
import com.cjl.self_schedule.appeal.service.AppealService;
import com.cjl.self_schedule.appeal.vo.AppealVO;
import com.cjl.self_schedule.auth.entity.User;
import com.cjl.self_schedule.auth.service.UserService;
import com.cjl.self_schedule.common.controller.BaseController;
import com.cjl.self_schedule.common.vo.Result;
import com.cjl.self_schedule.feedback.dto.FeedbackReplyDTO;
import com.cjl.self_schedule.feedback.entity.Feedback;
import com.cjl.self_schedule.feedback.service.FeedbackService;
import com.cjl.self_schedule.feedback.vo.FeedbackVO;
import com.cjl.self_schedule.message.service.MessageService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
@Api(tags = "管理员后台")
public class AdminController extends BaseController {

    private static final int ONLINE_THRESHOLD_MINUTES = 15;
    private static final String USER_ACTIVE_PREFIX = "user:active:";

    @Autowired
    private UserService userService;

    @Autowired
    private FeedbackService feedbackService;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private MessageService messageService;

    @Autowired
    private AppealService appealService;

    private Result<?> checkAdmin(HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        User user = userService.getById(userId);
        if (user == null) {
            return Result.error(404, "用户不存在");
        }
        requireAdmin(user.getRole());
        return null; // 检查通过，返回 null 表示无错误
    }

    @GetMapping("/feedbacks")
    @ApiOperation(value = "获取反馈列表")
    public Result<Map<String, Object>> getFeedbacks(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) Integer status,
            HttpServletRequest request) {
        Result<?> check = checkAdmin(request);
        if (check != null) return (Result<Map<String, Object>>) check;

        Page<Feedback> page = new Page<>(pageNum, Math.min(pageSize, 100));
        QueryWrapper<Feedback> query = new QueryWrapper<>();
        if (status != null) {
            query.eq("status", status);
        }
        query.orderByDesc("created_time");

        Page<Feedback> result = feedbackService.page(page, query);

        // 批量查询用户信息，避免 N+1
        List<Feedback> feedbacks = result.getRecords();
        Map<Long, User> userMap = batchGetUsers(feedbacks.stream()
                .map(Feedback::getUserId).distinct().collect(Collectors.toList()));

        List<FeedbackVO> voList = feedbacks.stream()
                .map(f -> convertFeedbackVO(f, userMap))
                .collect(Collectors.toList());

        Map<String, Object> data = new HashMap<>();
        data.put("records", voList);
        data.put("total", result.getTotal());
        data.put("pageNum", pageNum);
        data.put("pageSize", pageSize);

        return Result.success(data);
    }

    @PutMapping("/feedbacks/{id}/reply")
    @ApiOperation(value = "回复反馈")
    public Result<Void> replyFeedback(
            @PathVariable Long id,
            @RequestBody FeedbackReplyDTO dto,
            HttpServletRequest request) {
        Result<?> check = checkAdmin(request);
        if (check != null) return (Result<Void>) check;

        Feedback feedback = feedbackService.getById(id);
        if (feedback == null) {
            return Result.error(404, "反馈不存在");
        }

        if (dto.getStatus() != null) {
            feedback.setStatus(dto.getStatus());
        }
        if (dto.getAdminReply() != null) {
            feedback.setAdminReply(dto.getAdminReply().trim());
            feedback.setUserRead(0);
        }

        feedbackService.updateById(feedback);
        return Result.success("回复成功", null);
    }

    @GetMapping("/users/online")
    @ApiOperation(value = "查看用户在线状态")
    public Result<Map<String, Object>> getUserOnlineStatus(HttpServletRequest request) {
        Result<?> check = checkAdmin(request);
        if (check != null) return (Result<Map<String, Object>>) check;

        // 只查询有登录记录的用户，而非全部用户
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.isNotNull("last_login_time");
        queryWrapper.orderByDesc("last_login_time");
        List<User> users = userService.list(queryWrapper);

        long now = System.currentTimeMillis();
        long thresholdMs = ONLINE_THRESHOLD_MINUTES * 60 * 1000L;

        // 批量获取 Redis 活跃时间
        List<String> keys = users.stream()
                .map(u -> USER_ACTIVE_PREFIX + u.getId())
                .collect(Collectors.toList());
        List<String> redisValues = keys.isEmpty() ? Collections.emptyList()
                : redisTemplate.opsForValue().multiGet(keys);

        long onlineCount = 0;
        long todayActiveCount = 0;
        long totalActive = 0;
        LocalDateTime today = LocalDateTime.now().toLocalDate().atStartOfDay();

        List<Map<String, Object>> userList = new ArrayList<>();
        for (int i = 0; i < users.size(); i++) {
            User u = users.get(i);
            Map<String, Object> map = buildUserStatusMap(u, now, thresholdMs,
                    redisValues != null && i < redisValues.size() ? redisValues.get(i) : null);

            if ((boolean) map.get("isOnline")) onlineCount++;
            if (u.getLastLoginTime() != null && !u.getLastLoginTime().isBefore(today)) {
                todayActiveCount++;
            }
            if (u.getLastLoginTime() != null) totalActive++;
            userList.add(map);
        }

        Map<String, Object> data = new HashMap<>();
        data.put("onlineCount", onlineCount);
        data.put("todayActiveCount", todayActiveCount);
        data.put("totalActive", totalActive);
        data.put("totalUsers", users.size());
        data.put("onlineThresholdMinutes", ONLINE_THRESHOLD_MINUTES);
        data.put("users", userList);

        return Result.success(data);
    }

    @GetMapping("/users")
    @ApiOperation(value = "分页查询用户列表", notes = "支持按用户名/昵称搜索，返回分页数据和在线统计")
    public Result<Map<String, Object>> getUsers(
            @ApiParam("搜索关键词") @RequestParam(required = false) String keyword,
            @ApiParam("页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @ApiParam("每页大小") @RequestParam(defaultValue = "20") Integer pageSize,
            HttpServletRequest request) {
        Result<?> check = checkAdmin(request);
        if (check != null) return (Result<Map<String, Object>>) check;

        long now = System.currentTimeMillis();
        long thresholdMs = ONLINE_THRESHOLD_MINUTES * 60 * 1000L;

        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        if (keyword != null && !keyword.trim().isEmpty()) {
            String kw = keyword.trim();
            queryWrapper.and(q -> q.like("username", kw).or().like("nickname", kw));
        }
        queryWrapper.orderByDesc("created_time");

        Page<User> page = new Page<>(pageNum, Math.min(pageSize, 100));
        Page<User> userPage = userService.page(page, queryWrapper);
        List<User> users = userPage.getRecords();

        // 批量获取 Redis 活跃时间
        List<String> keys = users.stream()
                .map(u -> USER_ACTIVE_PREFIX + u.getId())
                .collect(Collectors.toList());
        List<String> redisValues = keys.isEmpty() ? Collections.emptyList()
                : redisTemplate.opsForValue().multiGet(keys);

        long onlineCount = 0;
        long todayActiveCount = 0;
        long totalActive = 0;
        long totalUsers = userPage.getTotal();
        LocalDateTime today = LocalDateTime.now().toLocalDate().atStartOfDay();

        List<Map<String, Object>> userList = new ArrayList<>();
        for (int i = 0; i < users.size(); i++) {
            User u = users.get(i);
            Map<String, Object> map = buildUserStatusMap(u, now, thresholdMs,
                    redisValues != null && i < redisValues.size() ? redisValues.get(i) : null);
            if ((boolean) map.get("isOnline")) onlineCount++;
            if (u.getLastLoginTime() != null && !u.getLastLoginTime().isBefore(today)) {
                todayActiveCount++;
            }
            if (u.getLastLoginTime() != null) totalActive++;
            userList.add(map);
        }

        Map<String, Object> data = new HashMap<>();
        data.put("users", userList);
        data.put("total", totalUsers);
        data.put("pageNum", pageNum);
        data.put("pageSize", pageSize);
        data.put("onlineCount", onlineCount);
        data.put("todayActiveCount", todayActiveCount);
        data.put("totalActive", totalActive);
        data.put("totalUsers", totalUsers);
        data.put("onlineThresholdMinutes", ONLINE_THRESHOLD_MINUTES);

        return Result.success(data);
    }

    private Map<String, Object> buildUserStatusMap(User u, long now, long thresholdMs, String redisValue) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", u.getId());
        map.put("username", u.getUsername());
        map.put("nickname", u.getNickname());
        map.put("avatarUrl", u.getAvatarUrl());
        map.put("role", u.getRole());
        map.put("isActive", u.getIsActive());
        map.put("status", u.getStatus());
        map.put("lastLoginTime", u.getLastLoginTime());
        map.put("createdTime", u.getCreatedTime());

        boolean online = false;
        Long lastActiveTime = null;

        if (redisValue != null) {
            try {
                long activeTs = Long.parseLong(redisValue);
                lastActiveTime = activeTs;
                online = (now - activeTs) < thresholdMs;
            } catch (NumberFormatException ignored) {
            }
        }

        if (!online && u.getLastLoginTime() != null) {
            long dbTs = u.getLastLoginTime().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
            if (lastActiveTime == null || dbTs > lastActiveTime) {
                lastActiveTime = dbTs;
            }
            online = (now - dbTs) < thresholdMs;
        }

        map.put("isOnline", online);
        if (lastActiveTime != null) {
            map.put("lastActiveTime", java.time.Instant.ofEpochMilli(lastActiveTime)
                    .atZone(ZoneId.systemDefault()).toLocalDateTime());
        } else {
            map.put("lastActiveTime", u.getLastLoginTime());
        }

        return map;
    }

    @PostMapping("/users/{id}/ban")
    @ApiOperation(value = "封禁用户", notes = "支持永久禁用和临时禁用")
    public Result<Void> banUser(
            @PathVariable Long id,
            @RequestBody BanUserDTO dto,
            HttpServletRequest request) {
        Result<?> check = checkAdmin(request);
        if (check != null) return (Result<Void>) check;

        User user = userService.getById(id);
        if (user == null) {
            return Result.error(404, "用户不存在");
        }

        if (user.getRole() != null && user.getRole() == 1) {
            return Result.error(400, "不能封禁管理员账号");
        }

        if (dto.getBanReason() == null || dto.getBanReason().trim().isEmpty()) {
            return Result.error(400, "禁用原因不能为空");
        }

        if (dto.getBanType() == null || (dto.getBanType() != 1 && dto.getBanType() != 2)) {
            return Result.error(400, "禁用类型无效，1=永久禁用 2=临时禁用");
        }

        if (dto.getBanType() == 2 && dto.getBanEndTime() == null) {
            return Result.error(400, "临时禁用必须指定到期时间");
        }

        if (dto.getBanType() == 2 && dto.getBanEndTime().isBefore(LocalDateTime.now())) {
            return Result.error(400, "到期时间不能早于当前时间");
        }

        Long adminId = getUserIdFromRequest(request);

        user.setStatus(dto.getBanType());
        user.setBanReason(dto.getBanReason().trim());
        user.setBanEndTime(dto.getBanType() == 1 ? null : dto.getBanEndTime());
        user.setBanOperator(adminId);
        user.setBanTime(LocalDateTime.now());
        user.setIsActive(0);
        userService.updateById(user);

        return Result.success("用户已封禁", null);
    }

    @PutMapping("/users/{id}/unban")
    @ApiOperation(value = "解封用户")
    public Result<Void> unbanUser(
            @PathVariable Long id,
            HttpServletRequest request) {
        Result<?> check = checkAdmin(request);
        if (check != null) return (Result<Void>) check;

        User user = userService.getById(id);
        if (user == null) {
            return Result.error(404, "用户不存在");
        }

        if (user.getStatus() == null || user.getStatus() == 0) {
            return Result.error(400, "该用户未被封禁");
        }

        user.setStatus(0);
        user.setBanReason(null);
        user.setBanEndTime(null);
        user.setBanOperator(null);
        user.setBanTime(null);
        user.setIsActive(1);
        userService.updateById(user);

        messageService.sendMessage(0L, user.getId(), "账号已解封",
                "管理员已解封您的账号，您现在可以正常登录使用。", 2);

        return Result.success("用户已解封", null);
    }

    @GetMapping("/appeals")
    @ApiOperation(value = "获取申诉列表")
    public Result<Map<String, Object>> getAppeals(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) Integer status,
            HttpServletRequest request) {
        Result<?> check = checkAdmin(request);
        if (check != null) return (Result<Map<String, Object>>) check;

        Page<Appeal> page = new Page<>(pageNum, Math.min(pageSize, 100));
        QueryWrapper<Appeal> query = new QueryWrapper<>();
        if (status != null) {
            query.eq("status", status);
        }
        query.orderByDesc("created_time");

        Page<Appeal> result = appealService.page(page, query);

        // 批量查询用户和管理员信息，避免 N+1
        List<Appeal> appeals = result.getRecords();
        Set<Long> userIds = new HashSet<>();
        for (Appeal a : appeals) {
            userIds.add(a.getUserId());
            if (a.getAuditAdminId() != null) {
                userIds.add(a.getAuditAdminId());
            }
        }
        Map<Long, User> userMap = batchGetUsers(new ArrayList<>(userIds));

        List<AppealVO> voList = appeals.stream()
                .map(a -> convertAppealVO(a, userMap))
                .collect(Collectors.toList());

        Map<String, Object> data = new HashMap<>();
        data.put("records", voList);
        data.put("total", result.getTotal());
        data.put("pageNum", pageNum);
        data.put("pageSize", pageSize);

        return Result.success(data);
    }

    @PutMapping("/appeals/{id}/audit")
    @ApiOperation(value = "审核申诉")
    public Result<Void> auditAppeal(
            @PathVariable Long id,
            @RequestBody AppealAuditDTO dto,
            HttpServletRequest request) {
        Result<?> check = checkAdmin(request);
        if (check != null) return (Result<Void>) check;

        if (dto.getStatus() == null || (dto.getStatus() != 1 && dto.getStatus() != 2)) {
            return Result.error(400, "审核状态无效，1=通过 2=驳回");
        }

        Long adminId = getUserIdFromRequest(request);

        try {
            appealService.auditAppeal(id, dto.getStatus(), dto.getAuditNote(), adminId);
            return Result.success(dto.getStatus() == 1 ? "申诉已通过，账号已解封" : "申诉已驳回", null);
        } catch (RuntimeException e) {
            return Result.error(400, e.getMessage());
        }
    }

    @GetMapping("/appeals/pending-count")
    @ApiOperation(value = "获取待审核申诉数量")
    public Result<Map<String, Long>> getPendingAppealCount(HttpServletRequest request) {
        Result<?> check = checkAdmin(request);
        if (check != null) return (Result<Map<String, Long>>) check;

        long count = appealService.count(new QueryWrapper<Appeal>().eq("status", 0));

        Map<String, Long> data = new HashMap<>();
        data.put("count", count);
        return Result.success(data);
    }

    @PostMapping("/messages/send")
    @ApiOperation(value = "发送消息（私发）")
    public Result<Void> sendMessage(
            @RequestBody Map<String, Object> params,
            HttpServletRequest request) {
        Result<?> check = checkAdmin(request);
        if (check != null) return (Result<Void>) check;

        Long recipientId = Long.valueOf(params.get("recipientId").toString());
        String title = (String) params.get("title");
        String content = (String) params.get("content");

        if (content == null || content.trim().isEmpty()) {
            return Result.error(400, "消息内容不能为空");
        }

        User recipient = userService.getById(recipientId);
        if (recipient == null) {
            return Result.error(404, "接收用户不存在");
        }

        Long adminId = getUserIdFromRequest(request);
        messageService.sendMessage(adminId, recipientId, title, content, 2);
        return Result.success("消息发送成功", null);
    }

    @PostMapping("/messages/broadcast")
    @ApiOperation(value = "群发消息")
    public Result<Void> broadcastMessage(
            @RequestBody Map<String, Object> params,
            HttpServletRequest request) {
        Result<?> check = checkAdmin(request);
        if (check != null) return (Result<Void>) check;

        Long adminId = getUserIdFromRequest(request);
        String title = (String) params.get("title");
        String content = (String) params.get("content");

        if (content == null || content.trim().isEmpty()) {
            return Result.error(400, "消息内容不能为空");
        }

        messageService.sendBroadcastMessage(adminId, title, content);
        return Result.success("群发消息成功", null);
    }

    @GetMapping("/feedbacks/stats")
    @ApiOperation(value = "反馈统计")
    public Result<Map<String, Object>> getFeedbackStats(HttpServletRequest request) {
        Result<?> check = checkAdmin(request);
        if (check != null) return (Result<Map<String, Object>>) check;

        long total = feedbackService.count();
        long pending = feedbackService.count(new QueryWrapper<Feedback>().eq("status", 0));
        long processed = feedbackService.count(new QueryWrapper<Feedback>().eq("status", 1));
        long closed = feedbackService.count(new QueryWrapper<Feedback>().eq("status", 2));

        Map<String, Object> data = new HashMap<>();
        data.put("total", total);
        data.put("pending", pending);
        data.put("processed", processed);
        data.put("closed", closed);

        return Result.success(data);
    }

    @GetMapping("/feedbacks/pending-count")
    @ApiOperation(value = "获取待处理反馈数量")
    public Result<Map<String, Long>> getPendingFeedbackCount(HttpServletRequest request) {
        Result<?> check = checkAdmin(request);
        if (check != null) return (Result<Map<String, Long>>) check;

        long count = feedbackService.count(new QueryWrapper<Feedback>().eq("status", 0));

        Map<String, Long> data = new HashMap<>();
        data.put("count", count);
        return Result.success(data);
    }

    // ========== 批量查询工具方法 ==========

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
     * 使用预加载的用户映射转换 FeedbackVO，避免 N+1 查询
     */
    private FeedbackVO convertFeedbackVO(Feedback feedback, Map<Long, User> userMap) {
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

    /**
     * 使用预加载的用户映射转换 AppealVO，避免 N+1 查询
     */
    private AppealVO convertAppealVO(Appeal appeal, Map<Long, User> userMap) {
        AppealVO vo = new AppealVO();
        vo.setId(appeal.getId());
        vo.setUserId(appeal.getUserId());
        vo.setContent(appeal.getContent());
        vo.setStatus(appeal.getStatus());
        vo.setAuditAdminId(appeal.getAuditAdminId());
        vo.setAuditTime(appeal.getAuditTime());
        vo.setAuditNote(appeal.getAuditNote());
        vo.setCreatedTime(appeal.getCreatedTime());

        User user = userMap.get(appeal.getUserId());
        if (user != null) {
            vo.setUsername(user.getUsername());
            vo.setNickname(user.getNickname());
            vo.setBanReason(user.getBanReason());
            vo.setUserStatus(user.getStatus());
            vo.setBanEndTime(user.getBanEndTime());
            vo.setBanTime(user.getBanTime());
        }

        if (appeal.getAuditAdminId() != null) {
            User admin = userMap.get(appeal.getAuditAdminId());
            if (admin != null) {
                vo.setAuditAdminName(admin.getNickname() != null && !admin.getNickname().isEmpty()
                        ? admin.getNickname() : admin.getUsername());
            }
        }

        return vo;
    }
}
