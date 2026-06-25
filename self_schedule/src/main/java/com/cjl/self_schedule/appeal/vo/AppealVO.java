package com.cjl.self_schedule.appeal.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AppealVO {
    private Long id;
    private Long userId;
    private String username;
    private String nickname;
    private String content;
    private Integer status;
    private Long auditAdminId;
    private String auditAdminName;
    private LocalDateTime auditTime;
    private String auditNote;
    private LocalDateTime createdTime;
    private String banReason;
    private Integer userStatus;
    private LocalDateTime banEndTime;
    private LocalDateTime banTime;
}
