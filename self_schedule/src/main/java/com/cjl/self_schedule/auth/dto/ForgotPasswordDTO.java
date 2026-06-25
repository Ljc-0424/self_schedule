package com.cjl.self_schedule.auth.dto;

import lombok.Data;

@Data
public class ForgotPasswordDTO {
    private String email;
    private String username;
}
