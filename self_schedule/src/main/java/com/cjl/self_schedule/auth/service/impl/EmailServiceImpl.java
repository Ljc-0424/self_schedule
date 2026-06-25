package com.cjl.self_schedule.auth.service.impl;

import com.cjl.self_schedule.auth.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class EmailServiceImpl implements EmailService {

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Value("${spring.mail.username:}")
    private String fromEmail;

    @Override
    @Async("asyncTaskExecutor")
    public void sendVerificationCode(String toEmail, String code) {
        if (mailSender == null) {
            log.error("邮件服务未配置，无法发送验证码。请配置 spring.mail.username 和 spring.mail.password");
            return;
        }

        if (fromEmail == null || fromEmail.isEmpty()) {
            log.error("发件邮箱地址未配置，无法发送验证码");
            return;
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("【SelfSchedule】密码重置验证码");
            helper.setText(buildEmailContent(code), true);
            mailSender.send(message);
            log.info("验证码邮件已发送至: {}", toEmail);
        } catch (MessagingException e) {
            log.error("发送验证码邮件失败: {}", e.getMessage(), e);
        }
    }

    private String buildEmailContent(String code) {
        StringBuilder sb = new StringBuilder();
        sb.append("<!DOCTYPE html><html><head><meta charset=\"UTF-8\">");
        sb.append("<style>");
        sb.append("body { font-family: 'Microsoft YaHei', sans-serif; background: #f5f5f5; padding: 20px; }");
        sb.append(".container { max-width: 500px; margin: 0 auto; background: #fff; border-radius: 12px; padding: 30px; box-shadow: 0 2px 12px rgba(0,0,0,0.08); }");
        sb.append(".header { text-align: center; margin-bottom: 24px; }");
        sb.append(".header h2 { color: #6366f1; margin: 0; }");
        sb.append(".code-box { text-align: center; background: #f0f0ff; border-radius: 8px; padding: 16px; margin: 20px 0; }");
        sb.append(".code { font-size: 32px; font-weight: bold; color: #6366f1; letter-spacing: 6px; }");
        sb.append(".tip { color: #999; font-size: 13px; text-align: center; margin-top: 16px; }");
        sb.append("</style></head><body>");
        sb.append("<div class=\"container\">");
        sb.append("<div class=\"header\"><h2>\uD83D\uDD10 密码重置验证码</h2></div>");
        sb.append("<p>您好，您正在重置 SelfSchedule 账户密码，验证码如下：</p>");
        sb.append("<div class=\"code-box\"><span class=\"code\">").append(code).append("</span></div>");
        sb.append("<p style=\"color:#666;\">验证码 <strong>5 分钟</strong>内有效，请勿泄露给他人。</p>");
        sb.append("<p class=\"tip\">如果这不是您的操作，请忽略此邮件。</p>");
        sb.append("</div></body></html>");
        return sb.toString();
    }
}
