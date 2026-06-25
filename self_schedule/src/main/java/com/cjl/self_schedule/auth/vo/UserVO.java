package com.cjl.self_schedule.auth.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.time.LocalDate;

@Data
public class UserVO {
    private Long id;
    private String username;
    private String nickname;
    private String email;
    private String avatarUrl;
    private String settings;
    private Integer isActive;
    private Integer status;
    private Integer role;
    private LocalDateTime lastLoginTime;
    private String occupation;
    private String phone;
    private String gender;
    private String city;
    private String hobbies;
    private String bio;
    private String weChatWebhookUrl;
    private String aiApiKey;
    private LocalDateTime createdTime;
    private LocalDateTime updatedTime;
    private LocalDate birthday;
    private Integer dailyFocusGoal;
    private Integer minEffectiveDuration;
}