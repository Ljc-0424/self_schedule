package com.cjl.self_schedule.appeal.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class BanInfoVO {
    private Integer status;
    private String banReason;
    private LocalDateTime banTime;
    private LocalDateTime banEndTime;
    private Integer appealStatus;
    private String auditNote;
    private String username;
}
