package com.cjl.self_schedule.appeal.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.cjl.self_schedule.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("user_appeal")
public class Appeal extends BaseEntity {
    private Long userId;
    private String content;
    private Integer status;
    private Long auditAdminId;
    private LocalDateTime auditTime;
    private String auditNote;
}
