package com.cjl.self_schedule.message.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.cjl.self_schedule.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("user_message")
public class Message extends BaseEntity {
    private Long senderId;
    private Long recipientId;
    private String title;
    private String content;
    private Integer messageType;
    private Integer isRead;
}
