package com.cjl.self_schedule.appeal.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class BanUserDTO {
    private Integer banType;
    private String banReason;
    private LocalDateTime banEndTime;
}
