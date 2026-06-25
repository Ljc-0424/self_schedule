package com.cjl.self_schedule.auth.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.cjl.self_schedule.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("`user`")
public class User extends BaseEntity {
    private String username;
    private String email;
    private String passwordHash;
    private String avatarUrl;
    private String nickname;
    private String settings;
    private Integer isActive;
    private Integer status;
    private String banReason;
    private LocalDateTime banEndTime;
    private Long banOperator;
    private LocalDateTime banTime;
    private Integer role;
    private LocalDateTime lastLoginTime;
    private String occupation;
    private LocalDate birthday;
    private String phone;
    private String gender;
    private String city;
    private String hobbies;
    private String bio;
    @TableField("wechat_webhook_url")
    private String weChatWebhookUrl;
    private String aiApiKey;
    private Integer dailyFocusGoal;
    private Integer minEffectiveDuration;
    private String openId;
}