package com.cjl.self_schedule.focus.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("focus_record")
public class FocusRecord {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long taskId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer duration;
    private Integer interruptions;
    private Integer focusScore;
    private String notes;
    private Integer status;
    private LocalDateTime createdTime;
}