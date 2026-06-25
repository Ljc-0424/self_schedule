package com.cjl.self_schedule.appeal.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cjl.self_schedule.appeal.dto.AppealAuditDTO;
import com.cjl.self_schedule.appeal.dto.AppealCreateDTO;
import com.cjl.self_schedule.appeal.entity.Appeal;
import com.cjl.self_schedule.appeal.service.AppealService;
import com.cjl.self_schedule.appeal.vo.AppealVO;
import com.cjl.self_schedule.appeal.vo.BanInfoVO;
import com.cjl.self_schedule.auth.entity.User;
import com.cjl.self_schedule.auth.service.UserService;
import com.cjl.self_schedule.common.controller.BaseController;
import com.cjl.self_schedule.common.vo.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/appeal")
@Api(tags = "申诉管理")
public class AppealController extends BaseController {

    @Autowired
    private AppealService appealService;

    @Autowired
    private UserService userService;

    @GetMapping("/ban-info")
    @ApiOperation(value = "获取封禁信息", notes = "根据用户名获取封禁信息，无需登录")
    public Result<BanInfoVO> getBanInfo(@RequestParam String username) {
        if (username == null || username.trim().isEmpty()) {
            return Result.error(400, "用户名不能为空");
        }

        BanInfoVO banInfo = appealService.getBanInfo(username.trim());
        if (banInfo == null) {
            return Result.error(404, "该账号未被封禁");
        }

        return Result.success(banInfo);
    }

    @PostMapping("/submit")
    @ApiOperation(value = "提交申诉", notes = "被封禁用户提交申诉，无需登录")
    public Result<Void> submitAppeal(@RequestBody AppealCreateDTO dto) {
        if (dto.getUsername() == null || dto.getUsername().trim().isEmpty()) {
            return Result.error(400, "用户名不能为空");
        }

        if (dto.getContent() == null || dto.getContent().trim().isEmpty()) {
            return Result.error(400, "申诉理由不能为空");
        }

        if (dto.getContent().trim().length() > 2000) {
            return Result.error(400, "申诉理由不能超过2000字");
        }

        try {
            appealService.submitAppeal(dto.getUsername().trim(), dto.getContent().trim());
            return Result.success("申诉提交成功，请耐心等待审核", null);
        } catch (RuntimeException e) {
            return Result.error(400, e.getMessage());
        }
    }

    @GetMapping("/admin/list")
    @ApiOperation(value = "管理员获取申诉列表")
    public Result<Map<String, Object>> getAppealList(
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
                .map(a -> convertVO(a, userMap))
                .collect(Collectors.toList());

        Map<String, Object> data = new HashMap<>();
        data.put("records", voList);
        data.put("total", result.getTotal());
        data.put("pageNum", pageNum);
        data.put("pageSize", pageSize);

        return Result.success(data);
    }

    @PutMapping("/admin/audit/{id}")
    @ApiOperation(value = "管理员审核申诉")
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

    @GetMapping("/admin/pending-count")
    @ApiOperation(value = "获取待审核申诉数量")
    public Result<Map<String, Long>> getPendingAppealCount(HttpServletRequest request) {
        Result<?> check = checkAdmin(request);
        if (check != null) return (Result<Map<String, Long>>) check;

        long count = appealService.count(new QueryWrapper<Appeal>().eq("status", 0));

        Map<String, Long> data = new HashMap<>();
        data.put("count", count);
        return Result.success(data);
    }

    private Result<?> checkAdmin(HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        User user = userService.getById(userId);
        if (user == null) {
            return Result.error(404, "用户不存在");
        }
        requireAdmin(user.getRole());
        return null; // 检查通过，返回 null 表示无错误
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
     * 使用预加载的用户映射转换 AppealVO
     */
    private AppealVO convertVO(Appeal appeal, Map<Long, User> userMap) {
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
