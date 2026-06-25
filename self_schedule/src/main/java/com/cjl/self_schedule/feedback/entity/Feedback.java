package com.cjl.self_schedule.feedback.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.cjl.self_schedule.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("feedback")
public class Feedback extends BaseEntity {
    private Long userId;
    private String category;
    private String title;
    private String content;
    private String contact;
    private Integer status;
    private String adminReply;
    private Integer userRead;
}
