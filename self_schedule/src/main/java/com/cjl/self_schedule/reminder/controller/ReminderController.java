package com.cjl.self_schedule.reminder.controller;

import com.cjl.self_schedule.reminder.entity.ReminderRecord;
import com.cjl.self_schedule.reminder.service.ReminderService;
import com.cjl.self_schedule.common.controller.BaseController;
import com.cjl.self_schedule.common.vo.PageResult;
import com.cjl.self_schedule.reminder.vo.ReminderRecordVO;
import com.cjl.self_schedule.common.vo.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/reminders")
@Api(tags = "提醒管理")
public class ReminderController extends BaseController {

    @Autowired
    private ReminderService reminderService;

    @Autowired
    private com.cjl.self_schedule.reminder.mapper.ReminderRecordMapper reminderRecordMapper;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @GetMapping("/pending")
    @ApiOperation(value = "获取待处理提醒", notes = "获取用户所有待处理的提醒记录")
    public Result<List<ReminderRecordVO>> getPendingReminders(HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        List<ReminderRecord> records = reminderService.getPendingReminders(userId);

        List<ReminderRecordVO> voList = records.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        return Result.success(voList);
    }

    @GetMapping("/all")
    @ApiOperation(value = "获取所有提醒", notes = "获取用户所有提醒记录（包含已读和已忽略）")
    public Result<PageResult<ReminderRecordVO>> getAllReminders(HttpServletRequest request,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {

        Long userId = getUserIdFromRequest(request);

        PageResult<ReminderRecord> pageResult = reminderService.getAllReminders(userId, page, pageSize);

        List<ReminderRecordVO> voList = pageResult.getData().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        PageResult<ReminderRecordVO> result = new PageResult<>();
        result.setData(voList);
        result.setPageNum(pageResult.getPageNum());
        result.setPageSize(pageResult.getPageSize());
        result.setTotal(pageResult.getTotal());
        result.setPages(pageResult.getPages());

        return Result.success(result);
    }

    @PutMapping("/{id}/status")
    @ApiOperation(value = "更新提醒状态", notes = "将提醒标记为已读或已忽略")
    public Result<Void> updateReminderStatus(@PathVariable Long id, @RequestParam String status,
            HttpServletRequest request) {
        if (!"READ".equals(status) && !"DISMISSED".equals(status)) {
            return Result.error(400, "无效的状态值，只能是 READ 或 DISMISSED");
        }

        Long userId = getUserIdFromRequest(request);
        com.cjl.self_schedule.reminder.entity.ReminderRecord record = reminderRecordMapper.selectById(id);
        if (record == null) {
            return Result.error(404, "提醒记录不存在");
        }
        if (!record.getUserId().equals(userId)) {
            return Result.error(403, "无权操作");
        }

        reminderService.updateReminderStatus(id, status);
        return Result.success(null);
    }

    @PutMapping("/read-all")
    @ApiOperation(value = "批量标记已读", notes = "将用户所有待处理提醒标记为已读")
    public Result<Void> markAllAsRead(HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        reminderService.markAllAsRead(userId);
        return Result.success(null);
    }

    @DeleteMapping("/{id}")
    @ApiOperation(value = "删除提醒", notes = "删除指定的提醒记录")
    public Result<Void> deleteReminder(@PathVariable Long id, HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        com.cjl.self_schedule.reminder.entity.ReminderRecord record = reminderRecordMapper.selectById(id);
        if (record == null) {
            return Result.error(404, "提醒记录不存在");
        }
        if (!record.getUserId().equals(userId)) {
            return Result.error(403, "无权操作");
        }

        reminderService.deleteReminder(id);
        return Result.success(null);
    }

    @PostMapping("/scan")
    @ApiOperation(value = "触发提醒扫描", notes = "手动触发提醒扫描（用于测试）")
    public Result<Map<String, Object>> triggerScan(HttpServletRequest request) {
        reminderService.scanAndTriggerReminders();

        Map<String, Object> result = new HashMap<>();
        result.put("message", "提醒扫描已触发");
        result.put("timestamp", LocalDateTime.now().format(FORMATTER));

        return Result.success(result);
    }

    @GetMapping("/stats")
    @ApiOperation(value = "获取提醒统计", notes = "获取用户提醒统计信息")
    public Result<Map<String, Object>> getReminderStats(HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        ReminderService.ReminderStats stats = reminderService.getReminderStats(userId);

        Map<String, Object> result = new HashMap<>();
        result.put("total", stats.getTotal());
        result.put("pending", stats.getPending());
        result.put("read", stats.getRead());
        result.put("dismissed", stats.getDismissed());
        result.put("expired", stats.getExpired());

        return Result.success(result);
    }

    @GetMapping
    @ApiOperation(value = "获取提醒列表", notes = "获取用户提醒记录（支持状态筛选）")
    public Result<PageResult<ReminderRecordVO>> getReminders(HttpServletRequest request,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {

        Long userId = getUserIdFromRequest(request);

        PageResult<ReminderRecord> pageResult;
        if ("ALL".equals(status) || status == null) {
            pageResult = reminderService.getAllReminders(userId, page, pageSize);
        } else {
            pageResult = reminderService.getRemindersByStatus(userId, status, page, pageSize);
        }

        List<ReminderRecordVO> voList = pageResult.getData().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        PageResult<ReminderRecordVO> result = new PageResult<>();
        result.setData(voList);
        result.setPageNum(pageResult.getPageNum());
        result.setPageSize(pageResult.getPageSize());
        result.setTotal(pageResult.getTotal());
        result.setPages(pageResult.getPages());

        return Result.success(result);
    }

    @DeleteMapping("/cleanup")
    @ApiOperation(value = "清理已完成提醒", notes = "批量清理已读、已忽略和已过期的提醒记录")
    public Result<Map<String, Object>> cleanupCompleted(HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);

        List<String> statusToDelete = List.of("READ", "DISMISSED", "EXPIRED");
        int deletedCount = reminderService.deleteByStatus(userId, statusToDelete);

        Map<String, Object> result = new HashMap<>();
        result.put("message", "清理完成");
        result.put("deletedCount", deletedCount);

        return Result.success(result);
    }

    @PutMapping("/expire")
    @ApiOperation(value = "标记已过期", notes = "将用户所有超时未读的提醒标记为已过期")
    public Result<Void> markAsExpired(HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        reminderService.markAsExpired(userId);
        return Result.success(null);
    }

    private ReminderRecordVO convertToVO(ReminderRecord record) {
        ReminderRecordVO vo = new ReminderRecordVO();
        vo.setId(record.getId());
        vo.setTaskId(record.getTaskId());
        vo.setTaskTitle(record.getTaskTitle());
        vo.setTriggerTime(record.getTriggerTime() != null ? record.getTriggerTime().format(FORMATTER) : null);
        vo.setTaskTime(record.getTaskTime() != null ? record.getTaskTime().format(FORMATTER) : null);
        vo.setReminderType(record.getReminderType());
        vo.setStatus(record.getStatus());
        vo.setMessage(record.getMessage());
        vo.setCreatedAt(record.getCreatedAt() != null ? record.getCreatedAt().format(FORMATTER) : null);
        return vo;
    }
}