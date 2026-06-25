package com.cjl.self_schedule.subtask.controller;

import com.cjl.self_schedule.subtask.entity.Subtask;
import com.cjl.self_schedule.subtask.service.SubtaskService;
import com.cjl.self_schedule.task.entity.Task;
import com.cjl.self_schedule.task.service.TaskService;
import com.cjl.self_schedule.common.controller.BaseController;
import com.cjl.self_schedule.common.vo.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/subtasks")
@Api(tags = "子任务管理")
public class SubtaskController extends BaseController {

    @Autowired
    private SubtaskService subtaskService;

    @Autowired
    private TaskService taskService;

    private boolean checkTaskOwnership(Long taskId, HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        Task task = taskService.getById(taskId);
        if (task == null || !task.getUserId().equals(userId)) {
            return false;
        }
        return true;
    }

    private boolean checkSubtaskAccess(Long subtaskId, HttpServletRequest request) {
        Subtask subtask = subtaskService.getById(subtaskId);
        if (subtask == null) {
            return false;
        }
        return checkTaskOwnership(subtask.getTaskId(), request);
    }

    @PostMapping
    @ApiOperation(value = "创建子任务", notes = "为指定任务创建子任务")
    public Result<Subtask> create(
            @ApiParam(value = "请求对象", required = true) HttpServletRequest request,
            @ApiParam(value = "父任务ID", required = true) @RequestParam Long taskId,
            @ApiParam(value = "子任务标题", required = true) @RequestParam String title) {
        if (!checkTaskOwnership(taskId, request)) {
            return Result.error(403, "无权操作");
        }
        Subtask subtask = subtaskService.create(taskId, title);
        return Result.success(subtask);
    }

    @PutMapping("/{id}")
    @ApiOperation(value = "更新子任务", notes = "更新子任务标题")
    public Result<Subtask> update(
            @ApiParam(value = "请求对象", required = true) HttpServletRequest request,
            @ApiParam(value = "子任务ID", required = true) @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        if (!checkSubtaskAccess(id, request)) {
            return Result.error(403, "无权操作");
        }
        String title = body.get("title");
        Subtask subtask = subtaskService.update(id, title);
        if (subtask == null) {
            return Result.error(404, "子任务不存在");
        }
        return Result.success(subtask);
    }

    @DeleteMapping("/{id}")
    @ApiOperation(value = "删除子任务", notes = "删除指定的子任务")
    public Result<Void> delete(
            @ApiParam(value = "请求对象", required = true) HttpServletRequest request,
            @ApiParam(value = "子任务ID", required = true) @PathVariable Long id) {
        if (!checkSubtaskAccess(id, request)) {
            return Result.error(403, "无权操作");
        }
        subtaskService.delete(id);
        return Result.success(null);
    }

    @PutMapping("/{id}/complete")
    @ApiOperation(value = "完成/取消完成子任务", notes = "切换子任务的完成状态")
    public Result<Subtask> complete(
            @ApiParam(value = "请求对象", required = true) HttpServletRequest request,
            @ApiParam(value = "子任务ID", required = true) @PathVariable Long id) {
        if (!checkSubtaskAccess(id, request)) {
            return Result.error(403, "无权操作");
        }
        Subtask subtask = subtaskService.complete(id);
        if (subtask == null) {
            return Result.error(404, "子任务不存在");
        }
        return Result.success(subtask);
    }

    @GetMapping("/task/{taskId}")
    @ApiOperation(value = "获取任务的子任务列表", notes = "获取指定任务下的所有子任务")
    public Result<List<Subtask>> getByTaskId(
            @ApiParam(value = "请求对象", required = true) HttpServletRequest request,
            @ApiParam(value = "任务ID", required = true) @PathVariable Long taskId) {
        if (!checkTaskOwnership(taskId, request)) {
            return Result.error(403, "无权操作");
        }
        List<Subtask> subtasks = subtaskService.findByTaskId(taskId);
        return Result.success(subtasks);
    }

    @GetMapping("/task/{taskId}/count")
    @ApiOperation(value = "获取子任务统计", notes = "获取指定任务的子任务数量和完成数量")
    public Result<Map<String, Integer>> getCount(
            @ApiParam(value = "请求对象", required = true) HttpServletRequest request,
            @ApiParam(value = "任务ID", required = true) @PathVariable Long taskId) {
        if (!checkTaskOwnership(taskId, request)) {
            return Result.error(403, "无权操作");
        }
        int total = subtaskService.countByTaskId(taskId);
        int completed = subtaskService.countCompletedByTaskId(taskId);
        return Result.success(Map.of("total", total, "completed", completed));
    }
}