package com.cjl.self_schedule.reminder.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cjl.self_schedule.reminder.entity.ReminderRecord;
import org.apache.ibatis.annotations.*;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface ReminderRecordMapper extends BaseMapper<ReminderRecord> {

    @Select("SELECT r.* FROM reminder_records r LEFT JOIN task t ON r.task_id = t.id WHERE r.user_id = #{userId} AND r.status = 'PENDING' AND t.status IN (0, 1) ORDER BY r.trigger_time DESC")
    List<ReminderRecord> findPendingByUserId(@Param("userId") Long userId);

    @Select("SELECT * FROM reminder_records WHERE user_id = #{userId} ORDER BY trigger_time DESC LIMIT #{offset}, #{pageSize}")
    List<ReminderRecord> findByUserId(@Param("userId") Long userId, @Param("offset") Integer offset, @Param("pageSize") Integer pageSize);

    @Select("SELECT COUNT(*) FROM reminder_records WHERE user_id = #{userId}")
    int countByUserId(@Param("userId") Long userId);

    @Update("UPDATE reminder_records SET status = #{status} WHERE id = #{id}")
    void updateStatus(@Param("id") Long id, @Param("status") String status);

    @Update("UPDATE reminder_records SET status = #{status} WHERE user_id = #{userId} AND status = 'SENT'")
    void updateAllStatusByUserId(@Param("userId") Long userId, @Param("status") String status);

    @Select("SELECT COUNT(*) FROM reminder_records WHERE task_id = #{taskId} AND trigger_time = #{triggerTime}")
    int countByTaskAndTime(@Param("taskId") Long taskId, @Param("triggerTime") LocalDateTime triggerTime);

    @Select("SELECT * FROM reminder_records WHERE task_id = #{taskId} AND trigger_time = #{triggerTime}")
    ReminderRecord findByTaskAndTime(@Param("taskId") Long taskId, @Param("triggerTime") LocalDateTime triggerTime);

    @Select("SELECT r.* FROM reminder_records r LEFT JOIN task t ON r.task_id = t.id WHERE r.status NOT IN ('DISMISSED', 'EXPIRED') AND (r.send_status = 0 OR r.send_status IS NULL) AND r.trigger_time <= #{now} AND t.status IN (0, 1) ORDER BY r.trigger_time ASC LIMIT #{limit}")
    List<ReminderRecord> findDueReminders(@Param("now") LocalDateTime now, @Param("limit") Integer limit);

    @Update("UPDATE reminder_records SET send_status = #{newStatus}, updated_at = #{updatedAt} WHERE id = #{id} AND send_status = #{expectedStatus}")
    int updateSendStatus(@Param("id") Long id, @Param("expectedStatus") Integer expectedStatus, @Param("newStatus") Integer newStatus, @Param("updatedAt") LocalDateTime updatedAt);

    @Select("SELECT r.* FROM reminder_records r LEFT JOIN task t ON r.task_id = t.id WHERE r.status NOT IN ('DISMISSED', 'EXPIRED') AND (r.send_status = 0 OR r.send_status IS NULL) AND r.retry_count < #{maxRetryCount} AND r.updated_at <= #{retryTimeThreshold} AND r.trigger_time <= #{currentTime} AND t.status IN (0, 1)")
    List<ReminderRecord> findRetryReminders(@Param("retryTimeThreshold") LocalDateTime retryTimeThreshold, @Param("maxRetryCount") Integer maxRetryCount, @Param("currentTime") LocalDateTime currentTime);

    @Delete("DELETE FROM reminder_records WHERE status = #{status} AND created_at <= #{time}")
    int deleteByStatusAndTime(@Param("status") String status, @Param("time") LocalDateTime time);

    @Update("UPDATE reminder_records SET status = 'EXPIRED' WHERE status = 'PENDING' AND trigger_time <= #{time}")
    int markExpiredReminders(@Param("time") LocalDateTime time);

    @Update("UPDATE reminder_records SET status = 'EXPIRED' WHERE user_id = #{userId} AND status = 'PENDING' AND trigger_time <= #{time}")
    int markExpiredByUserId(@Param("userId") Long userId, @Param("time") LocalDateTime time);

    @Select("SELECT COUNT(*) FROM reminder_records WHERE user_id = #{userId} AND status = #{status}")
    int countByStatus(@Param("userId") Long userId, @Param("status") String status);

    @Select("SELECT * FROM reminder_records WHERE user_id = #{userId} AND status = #{status} ORDER BY trigger_time DESC LIMIT #{offset}, #{pageSize}")
    List<ReminderRecord> findByStatus(@Param("userId") Long userId, @Param("status") String status, @Param("offset") Integer offset, @Param("pageSize") Integer pageSize);

    @Select("SELECT COUNT(*) FROM reminder_records WHERE user_id = #{userId} AND status = #{status}")
    int countByStatusTotal(@Param("userId") Long userId, @Param("status") String status);

    @Delete("<script>DELETE FROM reminder_records WHERE user_id = #{userId} AND status IN <foreach item='item' index='index' collection='status' open='(' separator=',' close=')'>#{item}</foreach></script>")
    int deleteByStatus(@Param("userId") Long userId, @Param("status") List<String> status);

    @Update("CREATE INDEX IF NOT EXISTS idx_reminder_send_trigger ON reminder_records (send_status, trigger_time)")
    void createIndexOnSendStatusAndTriggerTime();

    @Update("CREATE INDEX IF NOT EXISTS idx_reminder_task_trigger ON reminder_records (task_id, trigger_time)")
    void createIndexOnTaskIdAndTriggerTime();

    @Update("CREATE INDEX IF NOT EXISTS idx_reminder_user_status ON reminder_records (user_id, status)")
    void createIndexOnUserIdAndStatus();
}