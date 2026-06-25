package com.cjl.self_schedule.subtask.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cjl.self_schedule.subtask.entity.Subtask;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SubtaskMapper extends BaseMapper<Subtask> {
    
    @Select("SELECT * FROM subtask WHERE task_id = #{taskId} ORDER BY sort_order ASC")
    List<Subtask> findByTaskId(@Param("taskId") Long taskId);

    @Select("SELECT COUNT(*) FROM subtask WHERE task_id = #{taskId}")
    int countByTaskId(@Param("taskId") Long taskId);

    @Select("SELECT COUNT(*) FROM subtask WHERE task_id = #{taskId} AND status = 2")
    int countCompletedByTaskId(@Param("taskId") Long taskId);
}