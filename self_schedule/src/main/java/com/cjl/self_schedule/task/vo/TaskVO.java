package com.cjl.self_schedule.task.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class TaskVO {
    private Long id;
    private Long userId;
    private String title;
    private String description;
    private Integer priority;
    private Integer status;
    private LocalDateTime deadline;
    private LocalDateTime remindTime;
    private Integer estimatedSeconds;
    private Integer actualSeconds;
    private String category;
    private List<String> tags;
    private String reminderConfig;
    private Boolean isAiGenerated;
    private String recurrenceRule;
    private LocalDateTime recurrenceEndDate;
    private LocalDateTime createdTime;
    private LocalDateTime updatedTime;
    private LocalDateTime completedTime;
}