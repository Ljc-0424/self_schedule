package com.cjl.self_schedule.subtask.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.cjl.self_schedule.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("subtask")
public class Subtask extends BaseEntity {
    private Long taskId;
    private String title;
    private Integer status;
    private Integer sortOrder;
    private LocalDateTime completedTime;
}