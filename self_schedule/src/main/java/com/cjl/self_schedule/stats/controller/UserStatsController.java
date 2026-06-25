package com.cjl.self_schedule.stats.controller;

import com.cjl.self_schedule.common.controller.BaseController;
import com.cjl.self_schedule.common.vo.Result;
import com.cjl.self_schedule.focus.entity.FocusRecord;
import com.cjl.self_schedule.focus.service.FocusRecordService;
import com.cjl.self_schedule.stats.service.UserStatsService;
import com.cjl.self_schedule.stats.vo.UserStatsVO;
import com.cjl.self_schedule.task.entity.Task;
import com.cjl.self_schedule.task.service.TaskService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/user/stats")
@Api(tags = "用户统计")
public class UserStatsController extends BaseController {

    @Autowired
    private UserStatsService userStatsService;

    @Autowired
    private FocusRecordService focusRecordService;

    @Autowired
    private TaskService taskService;

    @GetMapping
    @ApiOperation(value = "获取用户统计数据", notes = "获取当前用户的专注统计、任务统计、打卡数据和勋章信息")
    public Result<UserStatsVO> getUserStats(@ApiParam(value = "请求对象", required = true) HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        UserStatsVO stats = userStatsService.getUserStats(userId);
        return Result.success(stats);
    }

    @GetMapping("/focus-by-category")
    @ApiOperation(value = "获取按任务分类的专注时间统计", notes = "根据时间范围统计各任务分类的专注时间，用于饼图展示")
    public Result<List<Map<String, Object>>> getFocusTimeByCategory(
            HttpServletRequest request,
            @ApiParam(value = "时间范围类型: day/week/month/custom") @RequestParam(defaultValue = "week") String range,
            @ApiParam(value = "自定义开始日期(yyyy-MM-dd)") @RequestParam(required = false) String startDate,
            @ApiParam(value = "自定义结束日期(yyyy-MM-dd)") @RequestParam(required = false) String endDate) {

        Long userId = getUserIdFromRequest(request);

        LocalDateTime startTime;
        LocalDateTime endTime;
        LocalDate today = LocalDate.now();

        switch (range) {
            case "day":
                startTime = today.atStartOfDay();
                endTime = today.plusDays(1).atStartOfDay();
                break;
            case "month":
                startTime = today.withDayOfMonth(1).atStartOfDay();
                endTime = today.plusDays(1).atStartOfDay();
                break;
            case "custom":
                DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                if (startDate != null && endDate != null) {
                    startTime = LocalDate.parse(startDate, fmt).atStartOfDay();
                    endTime = LocalDate.parse(endDate, fmt).plusDays(1).atStartOfDay();
                } else {
                    startTime = today.withDayOfMonth(1).atStartOfDay();
                    endTime = today.plusDays(1).atStartOfDay();
                }
                break;
            default:
                java.time.DayOfWeek dow = today.getDayOfWeek();
                startTime = today.minusDays(dow.getValue() - 1).atStartOfDay();
                endTime = today.plusDays(1).atStartOfDay();
                break;
        }

        com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<FocusRecord> query =
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
        query.eq("user_id", userId)
             .eq("status", 1)
             .ge("start_time", startTime)
             .lt("start_time", endTime);

        List<FocusRecord> records = focusRecordService.list(query);

        Set<Long> taskIds = records.stream()
                .map(FocusRecord::getTaskId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Map<Long, String> taskCategoryMap = new HashMap<>();
        if (!taskIds.isEmpty()) {
            List<Task> tasks = taskService.listByIds(taskIds);
            for (Task task : tasks) {
                String cat = (task.getCategory() != null && !task.getCategory().isEmpty())
                        ? task.getCategory() : "未分类";
                taskCategoryMap.put(task.getId(), cat);
            }
        }

        Map<String, Long> categoryDurationMap = new LinkedHashMap<>();
        long uncategorizedTotal = 0;

        for (FocusRecord record : records) {
            long duration = record.getDuration() != null ? record.getDuration() : 0;
            if (record.getTaskId() != null && taskCategoryMap.containsKey(record.getTaskId())) {
                String cat = taskCategoryMap.get(record.getTaskId());
                categoryDurationMap.merge(cat, duration, Long::sum);
            } else {
                uncategorizedTotal += duration;
            }
        }

        if (uncategorizedTotal > 0) {
            categoryDurationMap.merge("未分类", uncategorizedTotal, Long::sum);
        }

        List<Map<String, Object>> result = new ArrayList<>();
        for (Map.Entry<String, Long> entry : categoryDurationMap.entrySet()) {
            Map<String, Object> item = new HashMap<>();
            item.put("category", entry.getKey());
            item.put("duration", entry.getValue());
            result.add(item);
        }

        result.sort((a, b) -> Long.compare((Long) b.get("duration"), (Long) a.get("duration")));

        return Result.success(result);
    }
}
