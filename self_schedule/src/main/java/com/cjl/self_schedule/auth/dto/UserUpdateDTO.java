package com.cjl.self_schedule.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

@Data
public class UserUpdateDTO {
    @Size(max = 50, message = "昵称长度不能超过50个字符")
    private String nickname;

    @Email(message = "请输入有效的邮箱地址")
    private String email;

    @Size(max = 500, message = "头像URL长度不能超过500个字符")
    private String avatarUrl;

    private String settings;

    @Size(max = 50, message = "职业长度不能超过50个字符")
    private String occupation;

    private LocalDate birthday;

    @Size(max = 20, message = "电话长度不能超过20个字符")
    private String phone;

    @Size(max = 10, message = "性别长度不能超过10个字符")
    private String gender;

    @Size(max = 50, message = "城市长度不能超过50个字符")
    private String city;

    @Size(max = 500, message = "爱好长度不能超过500个字符")
    private String hobbies;

    @Size(max = 500, message = "简介长度不能超过500个字符")
    private String bio;

    private String weChatWebhookUrl;

    private String aiApiKey;

    private Integer dailyFocusGoal;

    private Integer minEffectiveDuration;
}