package com.cjl.self_schedule.reminder.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("reminder_records")
public class ReminderRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;
    private Long taskId;
    private String taskTitle;
    private LocalDateTime triggerTime;
    private LocalDateTime taskTime;
    private String reminderType;
    private String status;
    private String message;
    private String channel;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer retryCount;
    private Integer sendStatus;

    public ReminderRecord() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.status = "PENDING";
        this.channel = "SYSTEM";
        this.retryCount = 0;
        this.sendStatus = 0;
    }
}