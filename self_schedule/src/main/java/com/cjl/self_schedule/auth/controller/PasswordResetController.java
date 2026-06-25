package com.cjl.self_schedule.auth.controller;

import com.cjl.self_schedule.auth.dto.ChangePasswordDTO;
import com.cjl.self_schedule.auth.dto.ForgotPasswordDTO;
import com.cjl.self_schedule.auth.dto.ResetPasswordDTO;
import com.cjl.self_schedule.auth.entity.User;
import com.cjl.self_schedule.auth.service.EmailService;
import com.cjl.self_schedule.auth.service.UserService;
import com.cjl.self_schedule.auth.service.VerificationCodeService;
import com.cjl.self_schedule.common.controller.BaseController;
import com.cjl.self_schedule.common.exception.BusinessException;
import com.cjl.self_schedule.common.utils.PasswordUtil;
import com.cjl.self_schedule.common.vo.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.security.SecureRandom;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@Api(tags = "密码管理")
public class PasswordResetController extends BaseController {

    @Autowired
    private UserService userService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private VerificationCodeService verificationCodeService;

    private static final int SEND_COOLDOWN_SECONDS = 60;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    @PostMapping("/forgot-password")
    @ApiOperation(value = "发送验证码", notes = "通过邮箱或用户名找回密码，向用户绑定的邮箱发送6位验证码，有效期5分钟")
    public Result<String> forgotPassword(@RequestBody ForgotPasswordDTO dto) {
        String email = dto.getEmail();
        String username = dto.getUsername();

        if ((email == null || email.trim().isEmpty()) && (username == null || username.trim().isEmpty())) {
            throw BusinessException.badRequest("请输入邮箱或用户名");
        }

        User user = null;
        if (email != null && !email.trim().isEmpty()) {
            user = userService.findByEmail(email.trim());
            if (user == null) {
                throw BusinessException.notFound("该邮箱未注册");
            }
        } else {
            user = userService.findByUsername(username.trim());
            if (user == null) {
                throw BusinessException.notFound("该用户名未注册");
            }
            if (user.getEmail() == null || user.getEmail().trim().isEmpty()) {
                throw BusinessException.badRequest("该用户未绑定邮箱，无法通过邮箱找回密码。请联系管理员重置密码");
            }
            email = user.getEmail().trim();
        }

        if (verificationCodeService.hasCode(email)) {
            long remaining = verificationCodeService.getCodeRemainingTime(email);
            if (remaining > (300 - SEND_COOLDOWN_SECONDS)) {
                throw new BusinessException(429, "验证码已发送，请稍后再试");
            }
        }

        String code = generateCode();
        verificationCodeService.saveCode(email, code);
        emailService.sendVerificationCode(email, code);

        log.info("验证码已发送至: {}", email);
        return Result.success("验证码已发送到您的邮箱", email);
    }

    @PostMapping("/verify-code")
    @ApiOperation(value = "验证验证码", notes = "验证邮箱验证码是否正确")
    public Result<Void> verifyCode(@RequestBody ResetPasswordDTO dto) {
        String email = dto.getEmail();
        String code = dto.getCode();

        if (email == null || email.trim().isEmpty() || code == null || code.trim().isEmpty()) {
            throw BusinessException.badRequest("邮箱和验证码不能为空");
        }

        if (!verificationCodeService.checkCode(email.trim(), code.trim())) {
            throw BusinessException.badRequest("验证码错误或已过期");
        }

        return Result.success("验证成功", null);
    }

    @PostMapping("/reset-password")
    @ApiOperation(value = "重置密码", notes = "通过验证码重置密码")
    public Result<Void> resetPassword(@Valid @RequestBody ResetPasswordDTO dto) {
        String email = dto.getEmail().trim();
        String code = dto.getCode().trim();
        String newPassword = dto.getNewPassword();

        if (!verificationCodeService.verifyCode(email, code)) {
            throw BusinessException.badRequest("验证码错误或已过期，请重新获取");
        }

        User user = userService.findByEmail(email);
        if (user == null) {
            throw BusinessException.notFound("用户不存在");
        }

        user.setPasswordHash(PasswordUtil.encryptPassword(newPassword));
        userService.updateById(user);

        log.info("密码重置成功: {}", email);
        return Result.success("密码重置成功，请登录", null);
    }

    @PostMapping("/change-password")
    @ApiOperation(value = "修改密码", notes = "已登录用户通过旧密码修改新密码")
    public Result<Void> changePassword(@Valid @RequestBody ChangePasswordDTO dto, HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);

        String oldPassword = dto.getOldPassword();
        String newPassword = dto.getNewPassword();

        if (oldPassword.equals(newPassword)) {
            throw BusinessException.badRequest("新密码不能与旧密码相同");
        }

        User user = userService.getById(userId);
        if (user == null) {
            throw BusinessException.notFound("用户不存在");
        }

        if (!PasswordUtil.verifyPassword(oldPassword, user.getPasswordHash())) {
            throw BusinessException.badRequest("旧密码错误");
        }

        user.setPasswordHash(PasswordUtil.encryptPassword(newPassword));
        userService.updateById(user);

        log.info("密码修改成功: userId={}", userId);
        return Result.success("密码修改成功", null);
    }

    private String generateCode() {
        int code = 100000 + SECURE_RANDOM.nextInt(900000);
        return String.valueOf(code);
    }
}
