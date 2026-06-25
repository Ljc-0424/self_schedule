package com.cjl.self_schedule.task.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.cjl.self_schedule.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("task")
public class Task extends BaseEntity {
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
    private String tags;
    private String reminderConfig;
    private Integer isAiGenerated;
    private String recurrenceRule;
    private LocalDateTime recurrenceEndDate;
    private LocalDateTime completedTime;
}