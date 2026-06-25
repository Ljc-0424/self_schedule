package com.cjl.self_schedule.auth.service;

public interface EmailService {

    void sendVerificationCode(String toEmail, String code);
}
