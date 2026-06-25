package com.cjl.self_schedule.task.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cjl.self_schedule.task.entity.Task;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface TaskMapper extends BaseMapper<Task> {

    @Select("SELECT AUTO_INCREMENT FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'task'")
    Long getAutoIncrement();

    @Update("ALTER TABLE task AUTO_INCREMENT = #{nextId}")
    void resetAutoIncrement(@Param("nextId") Long nextId);

    @Select("SELECT * FROM task WHERE user_id = #{userId}")
    List<Task> selectByUserId(@Param("userId") Long userId);

    @Select("SELECT * FROM task WHERE user_id = #{userId} AND status = #{status}")
    List<Task> selectByUserIdAndStatus(@Param("userId") Long userId, @Param("status") Integer status);

    @Select("SELECT * FROM task WHERE status IN (0, 1) AND deadline IS NOT NULL AND deadline <= #{now}")
    List<Task> selectPendingTasks(@Param("now") LocalDateTime now);

    @Select("SELECT * FROM task WHERE user_id = #{userId} AND status IN (0, 1) AND deadline IS NOT NULL AND deadline < #{now}")
    List<Task> selectOverdueTasks(@Param("userId") Long userId, @Param("now") LocalDateTime now);

    @Select("SELECT * FROM task WHERE status IN (0, 1) " +
            "AND ((remind_time IS NOT NULL AND remind_time >= #{startTime} AND remind_time <= #{endTime}) " +
            "OR (deadline IS NOT NULL AND deadline >= #{startTime} AND deadline <= #{endTime}))")
    List<Task> findTasksNeedReminder(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

    @Select("SELECT * FROM task WHERE status = 2 AND recurrence_rule IS NOT NULL AND recurrence_rule != '' " +
            "AND (recurrence_end_date IS NULL OR recurrence_end_date > NOW())")
    List<Task> findCompletedRecurringTasks();

    @Select("SELECT * FROM task WHERE status IN (0, 1) AND recurrence_rule IS NOT NULL AND recurrence_rule != '' " +
            "AND ((deadline IS NOT NULL AND deadline < #{thresholdTime}) " +
            "OR (remind_time IS NOT NULL AND remind_time < #{thresholdTime})) " +
            "AND (recurrence_end_date IS NULL OR recurrence_end_date > NOW())")
    List<Task> findExpiredRecurringTasks(@Param("thresholdTime") LocalDateTime thresholdTime);

    int countByUserTitleAndTime(@Param("userId") Long userId, @Param("title") String title,
                                 @Param("deadline") LocalDateTime deadline, @Param("remindTime") LocalDateTime remindTime);

    @Select("DELETE FROM task WHERE user_id = #{userId}")
    int deleteByUserId(@Param("userId") Long userId);

    @Delete("DELETE FROM task WHERE status = #{status} AND updated_time <= #{before}")
    int deleteByStatusAndTime(@Param("status") Integer status, @Param("before") LocalDateTime before);

    @Select("SELECT COUNT(*) FROM task WHERE user_id = #{userId} AND status = 2")
    Integer countCompletedTasks(@Param("userId") Long userId);

    @Select("SELECT COUNT(*) FROM task WHERE user_id = #{userId} AND status = 2 AND completed_time >= #{startTime} AND completed_time < #{endTime}")
    Integer countCompletedTasksInRange(@Param("userId") Long userId, @Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

    @Select("SELECT COUNT(*) FROM task WHERE user_id = #{userId} AND status IN (0, 1) AND created_time < #{endTime} AND (deadline IS NULL OR deadline < #{endTime})")
    Integer countActiveTasksBefore(@Param("userId") Long userId, @Param("endTime") LocalDateTime endTime);

    @Select("SELECT COUNT(DISTINCT category) FROM task WHERE user_id = #{userId} AND status = 2 AND category IS NOT NULL AND category != ''")
    Integer countDistinctCompletedCategories(@Param("userId") Long userId);

    @Update("CREATE INDEX IF NOT EXISTS idx_task_status_remind ON task (status, remind_time)")
    void createIndexOnStatusAndRemindTime();

    @Update("CREATE INDEX IF NOT EXISTS idx_task_status_deadline ON task (status, deadline)")
    void createIndexOnStatusAndDeadline();

    @Update("CREATE INDEX IF NOT EXISTS idx_task_user_status ON task (user_id, status)")
    void createIndexOnUserIdAndStatus();
}
