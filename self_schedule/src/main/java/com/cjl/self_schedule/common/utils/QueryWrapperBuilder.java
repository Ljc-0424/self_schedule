package com.cjl.self_schedule.common.utils;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cjl.self_schedule.task.dto.TaskQueryDTO;
import com.cjl.self_schedule.focus.dto.FocusRecordQueryDTO;
import com.cjl.self_schedule.task.entity.Task;
import com.cjl.self_schedule.focus.entity.FocusRecord;

public class QueryWrapperBuilder {

  public static QueryWrapper<Task> buildTaskQueryWrapper(Long userId, TaskQueryDTO queryDTO) {
    QueryWrapper<Task> queryWrapper = new QueryWrapper<>();
    queryWrapper.eq("user_id", userId);

    if (queryDTO.getCategory() != null && !queryDTO.getCategory().isEmpty()) {
      queryWrapper.eq("category", queryDTO.getCategory());
    }

    if (queryDTO.getStatusList() != null) {
      if (!queryDTO.getStatusList().isEmpty()) {
        queryWrapper.in("status", queryDTO.getStatusList());
      }
    } else {
      queryWrapper.in("status", 0, 1);
    }

    if (queryDTO.getPriorityList() != null && !queryDTO.getPriorityList().isEmpty()) {
      queryWrapper.in("priority", queryDTO.getPriorityList());
    }

    if (queryDTO.getDeadlineBefore() != null) {
      queryWrapper.and(w -> w.le("deadline", queryDTO.getDeadlineBefore())
          .or().le("remind_time", queryDTO.getDeadlineBefore()));
    }

    if (queryDTO.getKeyword() != null && !queryDTO.getKeyword().isEmpty()) {
      queryWrapper.and(w -> w.like("title", queryDTO.getKeyword())
          .or().like("description", queryDTO.getKeyword())
          .or().like("tags", queryDTO.getKeyword()));
    }

    queryWrapper.orderByAsc("priority")
            .orderByAsc("ISNULL(deadline)")
            .orderByAsc("deadline")
            .orderByAsc("remind_time")
            .orderByDesc("created_time");

    return queryWrapper;
  }

  public static QueryWrapper<FocusRecord> buildFocusRecordQueryWrapper(Long userId, FocusRecordQueryDTO queryDTO) {
    QueryWrapper<FocusRecord> queryWrapper = new QueryWrapper<>();
    queryWrapper.eq("user_id", userId);

    if (queryDTO.getTaskId() != null) {
      queryWrapper.eq("task_id", queryDTO.getTaskId());
    }
    if (queryDTO.getStartTimeAfter() != null) {
      queryWrapper.ge("start_time", queryDTO.getStartTimeAfter());
    }
    if (queryDTO.getEndTimeBefore() != null) {
      queryWrapper.le("end_time", queryDTO.getEndTimeBefore());
    }

    queryWrapper.orderByDesc("start_time");

    return queryWrapper;
  }
}