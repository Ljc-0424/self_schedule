package com.cjl.self_schedule.focus.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cjl.self_schedule.focus.dto.FocusRecordCreateDTO;
import com.cjl.self_schedule.focus.dto.FocusRecordQueryDTO;
import com.cjl.self_schedule.focus.entity.FocusRecord;
import com.cjl.self_schedule.focus.service.FocusRecordService;
import com.cjl.self_schedule.focus.vo.FocusRecordVO;
import com.cjl.self_schedule.common.controller.BaseController;
import com.cjl.self_schedule.common.converter.VOConverter;
import com.cjl.self_schedule.common.vo.PageResult;
import com.cjl.self_schedule.common.vo.Result;
import com.cjl.self_schedule.task.entity.Task;
import com.cjl.self_schedule.task.service.TaskService;
import com.cjl.self_schedule.task.vo.TaskVO;
import com.cjl.self_schedule.reminder.service.ReminderService;
import lombok.extern.slf4j.Slf4j;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/focus-records")
@Api(tags = "专注记录管理")
public class FocusRecordController extends BaseController {

    @Autowired
    private FocusRecordService focusRecordService;
    
    @Autowired
    private TaskService taskService;
    
    @Autowired
    private VOConverter voConverter;

    @Autowired(required = false)
    private ReminderService reminderService;

    @Autowired
    private com.cjl.self_schedule.common.service.CacheService cacheService;

    @GetMapping("/task-search")
    @ApiOperation(value = "搜索可关联的任务", notes = "搜索当前用户待办任务（用于专注记录关联），支持关键词匹配标题/描述/标签，只显示有预估完成时间的任务")
    public Result<PageResult<TaskVO>> searchTasks(@ApiParam(value = "请求对象", required = true) HttpServletRequest request,
                                                  @ApiParam(value = "搜索关键词") @RequestParam(required = false) String keyword,
                                                  @ApiParam(value = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
                                                  @ApiParam(value = "每页大小") @RequestParam(defaultValue = "10") Integer pageSize) {
        Long userId = getUserIdFromRequest(request);
        
        QueryWrapper<Task> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId)
                .eq("status", 0)
                .isNotNull("estimated_seconds")
                .ne("estimated_seconds", 0)
                .orderByDesc("deadline is not null")
                .orderByAsc("deadline");
        
        if (keyword != null && !keyword.trim().isEmpty()) {
            queryWrapper.and(w -> w.like("title", keyword)
                    .or().like("description", keyword)
                    .or().like("tags", keyword));
        }
        
        Page<Task> page = new Page<>(pageNum, pageSize);
        IPage<Task> taskPage = taskService.page(page, queryWrapper);
        
        List<TaskVO> taskVOs = taskPage.getRecords().stream().map(voConverter::convertToSimpleTaskVO).collect(Collectors.toList());
        
        PageResult<TaskVO> pageResult = PageResult.of(taskVOs, pageNum, pageSize, taskPage.getTotal());
        
        return Result.success(pageResult);
    }

    @PostMapping("/start")
    @ApiOperation(value = "开始专注", notes = "将关联的任务状态改为进行中(1)")
    public Result<?> startFocus(@ApiParam(value = "请求对象", required = true) HttpServletRequest request,
                               @ApiParam(value = "任务ID") @RequestParam(required = false) Long taskId) {
        Long userId = getUserIdFromRequest(request);
        
        if (taskId != null) {
            Task task = taskService.getById(taskId);
            if (task != null && task.getUserId().equals(userId)) {
                if (task.getStatus() == 0) {
                    task.setStatus(1);
                    task.setUpdatedTime(LocalDateTime.now());
                    taskService.updateById(task);
                    cacheService.clearTaskListCache(userId);
                    cacheService.clearTaskCategoriesCache(userId);
                }
            }
        }
        
        return Result.success("专注已开始");
    }
    
    @PostMapping
    @ApiOperation(value = "创建专注记录", notes = "创建专注记录，自动设置userId、createdTime。如果专注完成(status=1)且关联了任务，则自动将任务标记为完成")
    public Result<FocusRecordVO> createFocusRecord(@ApiParam(value = "请求对象", required = true) HttpServletRequest request,
                                                   @ApiParam(value = "专注记录信息", required = true) @RequestBody FocusRecordCreateDTO createDTO) {
        Long userId = getUserIdFromRequest(request);
        
        if (createDTO.getTaskId() != null) {
            Task task = taskService.getById(createDTO.getTaskId());
            if (task == null || !task.getUserId().equals(userId)) {
                return Result.error(403, "无权操作该任务");
            }
        }
        
        FocusRecord record = new FocusRecord();
        record.setUserId(userId);
        record.setTaskId(createDTO.getTaskId());
        
        record.setStartTime(createDTO.getStartTime() != null ? 
            createDTO.getStartTime().atZoneSameInstant(java.time.ZoneId.of("Asia/Shanghai")).toLocalDateTime() : LocalDateTime.now());
        record.setEndTime(createDTO.getEndTime() != null ? 
            createDTO.getEndTime().atZoneSameInstant(java.time.ZoneId.of("Asia/Shanghai")).toLocalDateTime() : LocalDateTime.now());
        
        record.setDuration(createDTO.getDuration());
        record.setInterruptions(createDTO.getInterruptions() != null ? createDTO.getInterruptions() : 0);
        record.setFocusScore(createDTO.getFocusScore());
        record.setNotes(createDTO.getNotes());
        record.setStatus(createDTO.getStatus());
        record.setCreatedTime(LocalDateTime.now());
        
        if (focusRecordService.save(record)) {
            if (createDTO.getStatus() == 1 && createDTO.getTaskId() != null) {
                Task task = taskService.getById(createDTO.getTaskId());
                if (task != null && task.getUserId().equals(userId)) {
                    task.setStatus(2);
                    task.setCompletedTime(LocalDateTime.now());
                    Integer currentActual = task.getActualSeconds() != null ? task.getActualSeconds() : 0;
                    task.setActualSeconds(currentActual + createDTO.getDuration());
                    task.setUpdatedTime(LocalDateTime.now());
                    taskService.updateById(task);
                    if (reminderService != null) {
                        reminderService.deleteTaskReminders(task.getId());
                    }

                    if (task.getRecurrenceRule() != null && !task.getRecurrenceRule().isEmpty()) {
                        Task nextTask = taskService.createNextRecurrence(task);
                        if (nextTask != null) {
                            log.info("专注完成触发重复任务，已创建下一次任务 - originalTaskId: {}, newTaskId: {}",
                                    task.getId(), nextTask.getId());
                        }
                    }
                }
            }
            
            FocusRecordVO vo = voConverter.convertToFocusRecordVO(record);
            return Result.success(vo);
        } else {
            return Result.error(500, "创建失败");
        }
    }

    @GetMapping
    @ApiOperation(value = "获取专注记录列表", notes = "根据筛选条件分页查询当前用户的专注记录历史")
    public Result<PageResult<FocusRecordVO>> getFocusRecords(@ApiParam(value = "请求对象", required = true) HttpServletRequest request,
                                                             @ApiParam(value = "查询条件") FocusRecordQueryDTO queryDTO) {
        Long userId = getUserIdFromRequest(request);

        IPage<FocusRecord> recordPage = focusRecordService.queryFocusRecordsPage(userId, queryDTO);

        // 使用批量转换，避免 N+1 查询关联的 Task
        List<FocusRecordVO> voList = voConverter.convertToFocusRecordVOList(recordPage.getRecords());

        int pageNum = queryDTO.getPageNum() != null ? queryDTO.getPageNum() : 1;
        int pageSize = queryDTO.getPageSize() != null ? queryDTO.getPageSize() : 20;
        PageResult<FocusRecordVO> pageResult = PageResult.of(voList, pageNum, pageSize, recordPage.getTotal());

        return Result.success(pageResult);
    }

    @GetMapping("/{id}")
    @ApiOperation(value = "获取专注记录详情", notes = "获取指定专注记录的详细信息，包含关联任务的完整信息")
    public Result<FocusRecordVO> getFocusRecordDetail(@ApiParam(value = "请求对象", required = true) HttpServletRequest request,
                                                      @ApiParam(value = "记录ID", required = true) @PathVariable Long id) {
        Long userId = getUserIdFromRequest(request);
        
        FocusRecord record = focusRecordService.getById(id);
        if (record == null) {
            return Result.error(404, "记录不存在");
        }
        
        if (!record.getUserId().equals(userId)) {
            return Result.error(403, "无权访问");
        }
        
        Task task = null;
        if (record.getTaskId() != null) {
            task = taskService.getById(record.getTaskId());
        }
        
        FocusRecordVO vo = voConverter.convertToDetailFocusRecordVO(record, task);
        return Result.success(vo);
    }

    @PutMapping("/{id}/notes")
    @ApiOperation(value = "更新专注记录笔记", notes = "更新指定专注记录的笔记内容")
    public Result<?> updateFocusRecordNotes(@ApiParam(value = "请求对象", required = true) HttpServletRequest request,
                                           @ApiParam(value = "记录ID", required = true) @PathVariable Long id,
                                           @ApiParam(value = "笔记内容") @RequestParam String notes) {
        Long userId = getUserIdFromRequest(request);
        
        FocusRecord record = focusRecordService.getById(id);
        if (record == null) {
            return Result.error(404, "记录不存在");
        }
        
        if (!record.getUserId().equals(userId)) {
            return Result.error(403, "无权更新");
        }
        
        record.setNotes(notes);
        
        if (focusRecordService.updateById(record)) {
            return Result.success("更新成功");
        } else {
            return Result.error(500, "更新失败");
        }
    }
    
    @DeleteMapping("/{id}")
    @ApiOperation(value = "删除专注记录", notes = "删除指定的专注记录")
    public Result<?> deleteFocusRecord(@ApiParam(value = "请求对象", required = true) HttpServletRequest request,
                                      @ApiParam(value = "记录ID", required = true) @PathVariable Long id) {
        Long userId = getUserIdFromRequest(request);
        
        FocusRecord record = focusRecordService.getById(id);
        if (record == null) {
            return Result.error(404, "记录不存在");
        }
        
        if (!record.getUserId().equals(userId)) {
            return Result.error(403, "无权删除");
        }
        
        if (focusRecordService.removeById(id)) {
            return Result.success("删除成功");
        } else {
            return Result.error(500, "删除失败");
        }
    }
}