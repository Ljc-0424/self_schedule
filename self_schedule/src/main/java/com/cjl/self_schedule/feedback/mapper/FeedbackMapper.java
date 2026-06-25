package com.cjl.self_schedule.feedback.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cjl.self_schedule.feedback.entity.Feedback;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;

@Mapper
public interface FeedbackMapper extends BaseMapper<Feedback> {

    @Delete("DELETE FROM feedback WHERE status = #{status} AND updated_time <= #{before}")
    int deleteByStatusAndTime(@Param("status") Integer status, @Param("before") LocalDateTime before);

    @Delete("DELETE FROM feedback WHERE status = #{status} AND user_read = 1 AND updated_time <= #{before}")
    int deleteProcessedAndRead(@Param("status") Integer status, @Param("before") LocalDateTime before);
}
