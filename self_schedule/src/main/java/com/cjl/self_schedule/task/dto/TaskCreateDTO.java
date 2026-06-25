package com.cjl.self_schedule.task.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class TaskCreateDTO {
    @NotBlank(message = "任务标题不能为空")
    @Size(max = 200, message = "任务标题不能超过200个字符")
    private String title;

    @Size(max = 2000, message = "任务描述不能超过2000个字符")
    private String description;

    private Integer priority;
    private Integer status;
    private LocalDateTime deadline;
    private LocalDateTime remindTime;
    private Integer estimatedSeconds;

    @Size(max = 50, message = "分类名称不能超过50个字符")
    private String category;

    private List<String> tags;
    private String reminderConfig;
    private String recurrenceRule;
    private LocalDateTime recurrenceEndDate;
    private Boolean isAiGenerated;
    private List<String> subtasks;
}
