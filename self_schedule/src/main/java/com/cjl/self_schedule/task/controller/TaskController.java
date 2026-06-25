package com.cjl.self_schedule.task.controller;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.cjl.self_schedule.task.dto.TaskCreateDTO;
import com.cjl.self_schedule.task.dto.TaskQueryDTO;
import com.cjl.self_schedule.task.dto.TaskUpdateDTO;
import com.cjl.self_schedule.task.entity.Task;
import com.cjl.self_schedule.task.service.TaskService;
import com.cjl.self_schedule.task.vo.TaskVO;
import com.cjl.self_schedule.common.controller.BaseController;
import com.cjl.self_schedule.common.converter.VOConverter;
import com.cjl.self_schedule.common.utils.JsonUtil;
import com.cjl.self_schedule.common.vo.PageResult;
import com.cjl.self_schedule.common.vo.Result;
import com.cjl.self_schedule.reminder.service.ReminderService;
import com.cjl.self_schedule.common.service.CacheService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/tasks")
@Api(tags = "任务管理")
public class TaskController extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(TaskController.class);

    @Autowired
    private TaskService taskService;

    @Autowired
    private VOConverter voConverter;

    @Autowired
    private CacheService cacheService;

    @Autowired(required = false)
    private ReminderService reminderService;

    @GetMapping("/categories")
    @ApiOperation(value = "获取任务分类列表", notes = "获取当前用户所有任务的分类（去重）")
    public Result<List<String>> getCategories(@ApiParam(value = "请求对象", required = true) HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        
        List<String> categories = cacheService.getCachedTaskCategories(userId);
        if (categories != null) {
            return Result.success(categories);
        }
        
        categories = taskService.getCategories(userId);
        cacheService.cacheTaskCategories(userId, categories);
        
        return Result.success(categories);
    }

    @GetMapping
    @ApiOperation(value = "获取任务列表", notes = "根据筛选条件分页查询当前用户的任务列表")
    @SuppressWarnings("unchecked")
    public Result<PageResult<TaskVO>> getTasks(@ApiParam(value = "请求对象", required = true) HttpServletRequest request,
            @ApiParam(value = "查询条件") TaskQueryDTO queryDTO) {
        Long userId = getUserIdFromRequest(request);

        int pageNum = queryDTO.getPageNum() != null ? queryDTO.getPageNum() : 1;
        int pageSize = queryDTO.getPageSize() != null ? queryDTO.getPageSize() : 20;
        
        StringBuilder keySuffix = new StringBuilder();
        if (queryDTO.getStatusList() != null && !queryDTO.getStatusList().isEmpty()) {
            keySuffix.append("statusList=").append(queryDTO.getStatusList()).append(";");
        }
        if (queryDTO.getPriorityList() != null && !queryDTO.getPriorityList().isEmpty()) {
            keySuffix.append("priorityList=").append(queryDTO.getPriorityList()).append(";");
        }
        if (queryDTO.getCategory() != null) {
            keySuffix.append("category=").append(queryDTO.getCategory()).append(";");
        }
        if (queryDTO.getDeadlineBefore() != null) {
            keySuffix.append("deadlineBefore=").append(queryDTO.getDeadlineBefore()).append(";");
        }
        if (queryDTO.getKeyword() != null) {
            keySuffix.append("keyword=").append(queryDTO.getKeyword()).append(";");
        }

        Object cachedResult = cacheService.getCachedTaskList(userId, pageNum, pageSize, keySuffix.toString());
        if (cachedResult != null && cachedResult instanceof PageResult) {
            return Result.success((PageResult<TaskVO>) cachedResult);
        }

        IPage<Task> taskPage = taskService.queryTasksPage(userId, queryDTO);

        List<TaskVO> taskVOs = taskPage.getRecords().stream()
                .map(voConverter::convertToTaskVO)
                .collect(Collectors.toList());

        PageResult<TaskVO> pageResult = PageResult.of(taskVOs, pageNum, pageSize, taskPage.getTotal());

        cacheService.cacheTaskList(userId, pageNum, pageSize, keySuffix.toString(), pageResult);

        return Result.success(pageResult);
    }

    @PostMapping
    @ApiOperation(value = "创建任务", notes = "创建新任务，自动设置userId、createdTime、updatedTime")
    public Result<TaskVO> createTask(@ApiParam(value = "请求对象", required = true) HttpServletRequest request,
            @ApiParam(value = "任务信息", required = true) @RequestBody TaskCreateDTO createDTO) {
        Long userId = getUserIdFromRequest(request);

        Task task = new Task();
        task.setUserId(userId);
        task.setTitle(createDTO.getTitle());
        task.setDescription(createDTO.getDescription());
        task.setPriority(createDTO.getPriority() != null ? createDTO.getPriority() : 0);
        task.setStatus(0);
        task.setDeadline(createDTO.getDeadline());
        task.setRemindTime(createDTO.getRemindTime());
        task.setEstimatedSeconds(createDTO.getEstimatedSeconds() != null ? createDTO.getEstimatedSeconds() : 0);
        task.setActualSeconds(0);
        task.setCategory(createDTO.getCategory());
        task.setTags(createDTO.getTags() != null && !createDTO.getTags().isEmpty() 
            ? JsonUtil.toJson(createDTO.getTags()) : null);
        task.setReminderConfig(createDTO.getReminderConfig() != null ? createDTO.getReminderConfig() : null);
        task.setIsAiGenerated(0);
        task.setRecurrenceRule(createDTO.getRecurrenceRule());
        task.setRecurrenceEndDate(createDTO.getRecurrenceEndDate());
        task.setCreatedTime(LocalDateTime.now());
        task.setUpdatedTime(LocalDateTime.now());

        if (taskService.save(task)) {
            cacheService.clearTaskCategoriesCache(userId);
            cacheService.clearTaskListCache(userId);
            
            if ((task.getRemindTime() != null || task.getDeadline() != null) && task.getStatus() < 2) {
                reminderService.updateTaskReminders(task);
            }
            
            TaskVO taskVO = voConverter.convertToTaskVO(task);
            return Result.success(taskVO);
        } else {
            return Result.error(500, "创建任务失败");
        }
    }

    @GetMapping("/{id}")
    @ApiOperation(value = "获取任务详情", notes = "获取指定任务的详细信息")
    public Result<TaskVO> getTaskById(@ApiParam(value = "请求对象", required = true) HttpServletRequest request,
            @ApiParam(value = "任务ID", required = true) @PathVariable Long id) {
        Long userId = getUserIdFromRequest(request);

        Task task = taskService.getById(id);
        if (task == null) {
            return Result.error(404, "任务不存在");
        }

        if (!task.getUserId().equals(userId)) {
            return Result.error(403, "无权访问");
        }

        TaskVO taskVO = voConverter.convertToTaskVO(task);

        return Result.success(taskVO);
    }

    @PutMapping("/{id}")
    @ApiOperation(value = "更新任务", notes = "更新指定任务的属性，自动更新updatedTime")
    public Result<TaskVO> updateTask(@ApiParam(value = "请求对象", required = true) HttpServletRequest request,
            @ApiParam(value = "任务ID", required = true) @PathVariable Long id,
            @ApiParam(value = "更新信息", required = true) @RequestBody TaskUpdateDTO updateDTO) {
        Long userId = getUserIdFromRequest(request);

        Task task = taskService.getById(id);
        if (task == null) {
            return Result.error(404, "任务不存在");
        }

        if (!task.getUserId().equals(userId)) {
            return Result.error(403, "无权修改");
        }

        UpdateWrapper<Task> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id", id);

        if (updateDTO.getTitle() != null) {
            updateWrapper.set("title", updateDTO.getTitle());
        }
        if (updateDTO.getDescription() != null) {
            updateWrapper.set("description", updateDTO.getDescription());
        }
        if (updateDTO.getPriority() != null) {
            updateWrapper.set("priority", updateDTO.getPriority());
        }
        if (updateDTO.getStatus() != null) {
            updateWrapper.set("status", updateDTO.getStatus());
            if (updateDTO.getStatus() == 2 && task.getCompletedTime() == null) {
                updateWrapper.set("completed_time", LocalDateTime.now());
            }
        }

        if (Boolean.TRUE.equals(updateDTO.getClearDeadline())) {
            updateWrapper.set("deadline", null);
        } else if (updateDTO.getDeadline() != null) {
            updateWrapper.set("deadline", updateDTO.getDeadline());
        }

        if (Boolean.TRUE.equals(updateDTO.getClearRemindTime())) {
            updateWrapper.set("remind_time", null);
        } else if (updateDTO.getRemindTime() != null) {
            updateWrapper.set("remind_time", updateDTO.getRemindTime());
        }
        if (updateDTO.getEstimatedSeconds() != null) {
            updateWrapper.set("estimated_seconds", updateDTO.getEstimatedSeconds());
        }
        if (updateDTO.getActualSeconds() != null) {
            updateWrapper.set("actual_seconds", updateDTO.getActualSeconds());
        }
        if (updateDTO.getCategory() != null) {
            updateWrapper.set("category", updateDTO.getCategory());
        }
        if (updateDTO.getTags() != null) {
            updateWrapper.set("tags", updateDTO.getTags().isEmpty() 
                ? null : JsonUtil.toJson(updateDTO.getTags()));
        }
        if (updateDTO.getReminderConfig() != null) {
            updateWrapper.set("reminder_config", updateDTO.getReminderConfig());
        }
        if (updateDTO.getRecurrenceRule() != null) {
            updateWrapper.set("recurrence_rule", updateDTO.getRecurrenceRule());
        }
        updateWrapper.set("updated_time", LocalDateTime.now());

        if (taskService.update(updateWrapper)) {
            cacheService.clearTaskCategoriesCache(userId);
            cacheService.clearTaskListCache(userId);

            Task updatedTask = taskService.getById(id);
            
            boolean timeChanged = !equalTime(task.getRemindTime(), updatedTask.getRemindTime()) 
                    || !equalTime(task.getDeadline(), updatedTask.getDeadline());
            boolean configChanged = !equalString(task.getReminderConfig(), updatedTask.getReminderConfig());
            
            if (updateDTO.getStatus() != null && updateDTO.getStatus() >= 2) {
                if (reminderService != null) {
                    reminderService.deleteTaskReminders(id);
                }
            } else if (timeChanged || configChanged) {
                reminderService.updateTaskReminders(updatedTask);
            }
            
            if (updateDTO.getStatus() != null && updateDTO.getStatus() == 2 
                    && (task.getStatus() == null || task.getStatus() != 2)
                    && updatedTask.getRecurrenceRule() != null 
                    && !updatedTask.getRecurrenceRule().isEmpty()) {
                Task nextTask = taskService.createNextRecurrence(updatedTask);
                if (nextTask != null) {
                    logger.info("重复任务更新为完成状态，已创建下一次任务 - originalTaskId: {}, newTaskId: {}", id, nextTask.getId());
                }
            }
            
            TaskVO taskVO = voConverter.convertToTaskVO(updatedTask);
            return Result.success(taskVO);
        } else {
            return Result.error(500, "更新失败");
        }
    }

    @PutMapping("/{id}/complete")
    @ApiOperation(value = "完成任务", notes = "将任务状态设为完成，自动设置completedTime和updatedTime")
    public Result<TaskVO> completeTask(@ApiParam(value = "请求对象", required = true) HttpServletRequest request,
            @ApiParam(value = "任务ID", required = true) @PathVariable Long id) {
        Long userId = getUserIdFromRequest(request);

        Task task = taskService.getById(id);
        if (task == null) {
            return Result.error(404, "任务不存在");
        }

        if (!task.getUserId().equals(userId)) {
            return Result.error(403, "无权操作");
        }

        if (taskService.completeTask(id)) {
            cacheService.clearTaskCategoriesCache(userId);
            cacheService.clearTaskListCache(userId);
            if (reminderService != null) {
                reminderService.deleteTaskReminders(id);
            }
            
            Task updatedTask = taskService.getById(id);
            if (updatedTask.getRecurrenceRule() != null && !updatedTask.getRecurrenceRule().isEmpty()) {
                Task nextTask = taskService.createNextRecurrence(updatedTask);
                if (nextTask != null) {
                    logger.info("重复任务完成，已创建下一次任务 - originalTaskId: {}, newTaskId: {}", updatedTask.getId(), nextTask.getId());
                }
            }
            
            TaskVO taskVO = voConverter.convertToTaskVO(taskService.getById(id));
            return Result.success(taskVO);
        } else {
            return Result.error(500, "操作失败");
        }
    }

    @DeleteMapping("/{id}")
    @ApiOperation(value = "删除任务", notes = "逻辑删除任务：已完成→已存档，待办→已取消，已取消/已存档→彻底删除")
    public Result<?> deleteTask(@ApiParam(value = "请求对象", required = true) HttpServletRequest request,
            @ApiParam(value = "任务ID", required = true) @PathVariable Long id) {
        Long userId = getUserIdFromRequest(request);

        Task task = taskService.getById(id);
        if (task == null) {
            return Result.error(404, "任务不存在");
        }

        if (!task.getUserId().equals(userId)) {
            return Result.error(403, "无权删除");
        }

        Integer status = task.getStatus();

        if (status == 2) {
            task.setStatus(4);
            task.setUpdatedTime(LocalDateTime.now());
            if (taskService.updateById(task)) {
                cacheService.clearTaskCategoriesCache(userId);
                cacheService.clearTaskListCache(userId);
                if (reminderService != null) {
                    reminderService.deleteTaskReminders(id);
                }
                return Result.success("已存档");
            } else {
                return Result.error(500, "存档失败");
            }
        } else if (status == 0 || status == 1) {
            task.setStatus(3);
            task.setUpdatedTime(LocalDateTime.now());
            if (taskService.updateById(task)) {
                cacheService.clearTaskCategoriesCache(userId);
                cacheService.clearTaskListCache(userId);
                if (reminderService != null) {
                    reminderService.deleteTaskReminders(id);
                }
                return Result.success("已取消");
            } else {
                return Result.error(500, "取消失败");
            }
        } else {
            if (taskService.removeById(id)) {
                cacheService.clearTaskCategoriesCache(userId);
                cacheService.clearTaskListCache(userId);
                if (reminderService != null) {
                    reminderService.deleteTaskReminders(id);
                }
                return Result.success("彻底删除成功");
            } else {
                return Result.error(500, "删除失败");
            }
        }
    }

    @PutMapping("/{id}/undo")
    @ApiOperation(value = "撤销任务", notes = "撤销任务状态：已存档→完成，已取消→待办，完成→待办")
    public Result<TaskVO> undoTask(@ApiParam(value = "请求对象", required = true) HttpServletRequest request,
            @ApiParam(value = "任务ID", required = true) @PathVariable Long id) {
        Long userId = getUserIdFromRequest(request);

        Task task = taskService.getById(id);
        if (task == null) {
            return Result.error(404, "任务不存在");
        }

        if (!task.getUserId().equals(userId)) {
            return Result.error(403, "无权操作");
        }

        Integer oldStatus = task.getStatus();
        
        if (taskService.undoTask(id)) {
            cacheService.clearTaskCategoriesCache(userId);
            cacheService.clearTaskListCache(userId);
            
            Task updatedTask = taskService.getById(id);
            if (updatedTask.getStatus() < 2 && reminderService != null
                    && (updatedTask.getRemindTime() != null || updatedTask.getDeadline() != null)) {
                reminderService.updateTaskReminders(updatedTask);
            }

            if (oldStatus == 2 && updatedTask.getRecurrenceRule() != null 
                    && !updatedTask.getRecurrenceRule().isEmpty()) {
                taskService.deleteNextRecurrence(updatedTask);
            }
            
            TaskVO taskVO = voConverter.convertToTaskVO(updatedTask);
            return Result.success(taskVO);
        } else {
            return Result.error(500, "撤销失败，当前状态不支持撤销");
        }
    }

    @DeleteMapping("/{id}/force")
    @ApiOperation(value = "彻底删除任务", notes = "物理删除任务")
    public Result<?> forceDeleteTask(@ApiParam(value = "请求对象", required = true) HttpServletRequest request,
            @ApiParam(value = "任务ID", required = true) @PathVariable Long id) {
        Long userId = getUserIdFromRequest(request);

        Task task = taskService.getById(id);
        if (task == null) {
            return Result.error(404, "任务不存在");
        }

        if (!task.getUserId().equals(userId)) {
            return Result.error(403, "无权删除");
        }

        if (taskService.removeById(id)) {
            cacheService.clearTaskCategoriesCache(userId);
            cacheService.clearTaskListCache(userId);
            if (reminderService != null) {
                reminderService.deleteTaskReminders(id);
            }
            return Result.success("删除成功");
        } else {
            return Result.error(500, "删除失败");
        }
    }
}