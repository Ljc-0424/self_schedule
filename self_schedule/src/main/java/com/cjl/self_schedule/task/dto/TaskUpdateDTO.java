package com.cjl.self_schedule.task.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TaskUpdateDTO {
    private String title;
    private String description;
    private Integer priority;
    private Integer status;
    private LocalDateTime deadline;
    private LocalDateTime remindTime;
    private Integer estimatedSeconds;
    private Integer actualSeconds;
    private String category;
    private String tags;
    private String reminderConfig;
    private Integer isAiGenerated;
    private String recurrenceRule;
    private LocalDateTime recurrenceEndDate;
    
    private Boolean clearDeadline;
    private Boolean clearRemindTime;
}