package com.cjl.self_schedule.auth.service;

public interface VerificationCodeService {

    void saveCode(String email, String code);

    boolean verifyCode(String email, String code);

    boolean checkCode(String email, String code);

    void deleteCode(String email);

    boolean hasCode(String email);

    long getCodeRemainingTime(String email);
}
