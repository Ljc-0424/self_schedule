package com.cjl.self_schedule.reminder.vo;

import lombok.Data;

@Data
public class ReminderRecordVO {

    private Long id;
    private Long taskId;
    private String taskTitle;
    private String triggerTime;
    private String taskTime;
    private String reminderType;
    private String status;
    private String message;
    private String createdAt;
}