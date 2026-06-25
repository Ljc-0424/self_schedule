package com.cjl.self_schedule.focus.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cjl.self_schedule.focus.dto.DailyFocusStat;
import com.cjl.self_schedule.focus.entity.FocusRecord;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface FocusRecordMapper extends BaseMapper<FocusRecord> {

    @Delete("DELETE FROM focus_record WHERE created_time <= #{before}")
    int deleteByCreatedTime(@Param("before") LocalDateTime before);

    @Select("SELECT IFNULL(SUM(duration), 0) FROM focus_record WHERE user_id = #{userId} AND status = 1")
    Long getTotalFocusDuration(@Param("userId") Long userId);

    @Select("SELECT IFNULL(SUM(duration), 0) FROM focus_record WHERE user_id = #{userId} AND status = 1 AND start_time >= #{startTime} AND start_time < #{endTime}")
    Long getFocusDurationInRange(@Param("userId") Long userId, @Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

    @Select("SELECT IFNULL(MAX(daily_total), 0) FROM (SELECT SUM(duration) as daily_total FROM focus_record WHERE user_id = #{userId} AND status = 1 GROUP BY DATE(start_time)) t")
    Long getMaxDailyFocusDuration(@Param("userId") Long userId);

    @Select("SELECT DATE(start_time) as focus_date, SUM(duration) as total_duration FROM focus_record WHERE user_id = #{userId} AND status = 1 GROUP BY DATE(start_time) ORDER BY focus_date")
    @Results({
        @Result(column = "focus_date", property = "focusDate"),
        @Result(column = "total_duration", property = "totalDuration")
    })
    List<DailyFocusStat> getDailyFocusTotals(@Param("userId") Long userId);
}
