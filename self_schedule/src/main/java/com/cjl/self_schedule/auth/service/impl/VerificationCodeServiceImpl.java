package com.cjl.self_schedule.auth.service.impl;

import com.cjl.self_schedule.auth.service.VerificationCodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class VerificationCodeServiceImpl implements VerificationCodeService {

    private static final String CODE_PREFIX = "verify:code:";
    private static final String ATTEMPT_PREFIX = "verify:attempt:";
    private static final long CODE_EXPIRE_MINUTES = 5;
    private static final int MAX_ATTEMPTS = 3;
    private static final long LOCK_EXPIRE_MINUTES = 15;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Override
    public void saveCode(String email, String code) {
        String key = CODE_PREFIX + email;
        redisTemplate.opsForValue().set(key, code, CODE_EXPIRE_MINUTES, TimeUnit.MINUTES);
        // 保存验证码时清除之前的尝试计数
        redisTemplate.delete(ATTEMPT_PREFIX + email);
    }

    @Override
    public boolean verifyCode(String email, String code) {
        // 检查是否被锁定
        if (isLocked(email)) {
            throw new com.cjl.self_schedule.common.exception.BusinessException(
                429, "验证码尝试次数过多，请" + LOCK_EXPIRE_MINUTES + "分钟后重试");
        }

        String key = CODE_PREFIX + email;
        String cachedCode = redisTemplate.opsForValue().get(key);
        if (cachedCode != null && cachedCode.equals(code)) {
            // 验证成功，清除验证码和尝试计数
            redisTemplate.delete(key);
            redisTemplate.delete(ATTEMPT_PREFIX + email);
            return true;
        }

        // 验证失败，增加尝试次数
        incrementAttempt(email);
        return false;
    }

    /**
     * 检查该 email 是否因尝试次数过多被锁定
     */
    private boolean isLocked(String email) {
        String attemptKey = ATTEMPT_PREFIX + email;
        String attemptStr = redisTemplate.opsForValue().get(attemptKey);
        if (attemptStr == null) {
            return false;
        }
        try {
            return Integer.parseInt(attemptStr) >= MAX_ATTEMPTS;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * 增加验证码尝试次数，超过阈值后锁定
     */
    private void incrementAttempt(String email) {
        String attemptKey = ATTEMPT_PREFIX + email;
        Long attempts = redisTemplate.opsForValue().increment(attemptKey);
        if (attempts != null && attempts == 1) {
            // 首次失败，设置过期时间（与锁定时间一致）
            redisTemplate.expire(attemptKey, LOCK_EXPIRE_MINUTES, TimeUnit.MINUTES);
        }
    }

    @Override
    public boolean checkCode(String email, String code) {
        String key = CODE_PREFIX + email;
        String cachedCode = redisTemplate.opsForValue().get(key);
        return cachedCode != null && cachedCode.equals(code);
    }

    @Override
    public void deleteCode(String email) {
        redisTemplate.delete(CODE_PREFIX + email);
        redisTemplate.delete(ATTEMPT_PREFIX + email);
    }

    @Override
    public boolean hasCode(String email) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(CODE_PREFIX + email));
    }

    @Override
    public long getCodeRemainingTime(String email) {
        Long ttl = redisTemplate.getExpire(CODE_PREFIX + email, TimeUnit.SECONDS);
        return ttl != null && ttl > 0 ? ttl : 0;
    }
}
