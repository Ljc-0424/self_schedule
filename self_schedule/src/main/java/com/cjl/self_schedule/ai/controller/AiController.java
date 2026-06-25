package com.cjl.self_schedule.ai.controller;

import com.cjl.self_schedule.ai.dto.AiTaskParseRequest;
import com.cjl.self_schedule.ai.service.AiTaskService;
import com.cjl.self_schedule.common.controller.BaseController;
import com.cjl.self_schedule.common.service.AsyncTaskService;
import com.cjl.self_schedule.common.vo.Result;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/ai")
public class AiController extends BaseController {

    private final AiTaskService aiTaskService;
    private final AsyncTaskService asyncTaskService;

    @Autowired
    public AiController(AiTaskService aiTaskService, AsyncTaskService asyncTaskService) {
        this.aiTaskService = aiTaskService;
        this.asyncTaskService = asyncTaskService;
    }

    @PostMapping({"/parse-task", "/parse", "/task/parse"})
    public Result<Map<String, Object>> parseTask(HttpServletRequest request, @RequestBody Map<String, String> body) {
        // 安全修复：始终从认证Token获取userId，禁止从请求体传入（防止IDOR）
        Long userId;
        try {
            userId = getUserIdFromRequest(request);
        } catch (Exception e) {
            log.warn("未登录或Token无效，拒绝AI请求: {}", e.getMessage());
            return Result.error(401, "请先登录");
        }

        String prompt = body.get("prompt");

        log.info("收到AI任务解析请求 - userId: {}, prompt: {}", userId, prompt);
        
        if (prompt == null || prompt.trim().isEmpty()) {
            return Result.error(400, "请输入任务描述");
        }
        
        AiTaskService.AiParseResult result = aiTaskService.parseAndCreateTasks(userId, prompt);
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", result.message());
        response.put("taskCreated", result.taskCreated());
        response.put("taskCount", result.taskCount());
        response.put("isAiGenerated", result.isAiGenerated());
        
        return Result.success(response);
    }

    @PostMapping("/parse-task/async")
    public Result<Map<String, Object>> parseTaskAsync(HttpServletRequest request, @RequestBody Map<String, String> body) {
        // 安全修复：始终从认证Token获取userId，禁止从请求体传入（防止IDOR）
        Long userId;
        try {
            userId = getUserIdFromRequest(request);
        } catch (Exception e) {
            log.warn("未登录或Token无效，拒绝AI请求");
            return Result.error(401, "请先登录");
        }

        String prompt = body.get("prompt");
        
        if (prompt == null || prompt.trim().isEmpty()) {
            return Result.error(400, "请输入任务描述");
        }
        
        try {
            String taskId = asyncTaskService.submitAiTask(userId, prompt);
            log.info("提交异步AI任务成功 - taskId: {}, userId: {}", taskId, userId);
            
            Map<String, Object> resultMap = new HashMap<>();
            resultMap.put("taskId", taskId);
            resultMap.put("message", "任务已提交，请通过taskId查询进度");
            
            return Result.success(resultMap);
        } catch (Exception e) {
            log.error("提交异步任务失败", e);
            return Result.error(500, "任务提交失败，请稍后重试");
        }
    }

    @GetMapping("/task/status/{taskId}")
    public Result<Map<String, Object>> getTaskStatus(@PathVariable String taskId) {
        try {
            AsyncTaskService.TaskStatus status = asyncTaskService.getTaskStatus(taskId);
            
            if (status == null) {
                return Result.error(404, "任务不存在或已过期");
            }
            
            Map<String, Object> resultMap = new HashMap<>();
            resultMap.put("taskId", taskId);
            resultMap.put("status", status.name());
            resultMap.put("statusDescription", status.getDescription());
            
            return Result.success(resultMap);
        } catch (Exception e) {
            log.error("查询任务状态失败", e);
            return Result.error(500, "查询失败，请稍后重试");
        }
    }

    @GetMapping("/task/result/{taskId}")
    public Result<Map<String, Object>> getTaskResult(@PathVariable String taskId) {
        try {
            AiTaskService.AiParseResult result = asyncTaskService.getTaskResult(taskId);
            
            if (result == null) {
                return Result.error(404, "任务不存在、未完成或已过期");
            }
            
            Map<String, Object> resultMap = new HashMap<>();
            resultMap.put("taskId", taskId);
            resultMap.put("message", result.message());
            resultMap.put("taskCreated", result.taskCreated());
            resultMap.put("taskCount", result.taskCount());
            resultMap.put("isAiGenerated", result.isAiGenerated());
            
            return Result.success(resultMap);
        } catch (Exception e) {
            log.error("获取任务结果失败", e);
            return Result.error(500, "获取结果失败，请稍后重试");
        }
    }

    @DeleteMapping("/task/{taskId}")
    public Result<Void> deleteTask(@PathVariable String taskId) {
        try {
            asyncTaskService.removeTask(taskId);
            log.info("删除异步任务记录 - taskId: {}", taskId);
            return Result.success(null);
        } catch (Exception e) {
            log.error("删除任务记录失败", e);
            return Result.error(500, "删除失败，请稍后重试");
        }
    }
}