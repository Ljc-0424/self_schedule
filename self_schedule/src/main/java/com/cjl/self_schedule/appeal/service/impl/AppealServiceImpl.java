package com.cjl.self_schedule.appeal.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cjl.self_schedule.appeal.entity.Appeal;
import com.cjl.self_schedule.appeal.mapper.AppealMapper;
import com.cjl.self_schedule.appeal.service.AppealService;
import com.cjl.self_schedule.appeal.vo.BanInfoVO;
import com.cjl.self_schedule.auth.entity.User;
import com.cjl.self_schedule.auth.service.UserService;
import com.cjl.self_schedule.common.exception.BusinessException;
import com.cjl.self_schedule.message.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AppealServiceImpl extends ServiceImpl<AppealMapper, Appeal> implements AppealService {

    @Autowired
    private UserService userService;

    @Autowired
    private MessageService messageService;

    @Override
    public BanInfoVO getBanInfo(String username) {
        User user = userService.findByUsername(username);
        if (user == null) {
            return null;
        }

        if (user.getStatus() == null || user.getStatus() == 0) {
            return null;
        }

        if (userService.checkAndUnbanExpired(user)) {
            return null;
        }

        BanInfoVO vo = new BanInfoVO();
        vo.setStatus(user.getStatus());
        vo.setBanReason(user.getBanReason());
        vo.setBanTime(user.getBanTime());
        vo.setBanEndTime(user.getBanEndTime());
        vo.setUsername(user.getUsername());

        QueryWrapper<Appeal> query = new QueryWrapper<>();
        query.eq("user_id", user.getId());
        query.orderByDesc("created_time");
        query.last("LIMIT 1");
        Appeal latestAppeal = getOne(query);

        if (latestAppeal != null) {
            vo.setAppealStatus(latestAppeal.getStatus());
            vo.setAuditNote(latestAppeal.getAuditNote());
        } else {
            vo.setAppealStatus(-1);
        }

        return vo;
    }

    @Override
    @Transactional
    public void submitAppeal(String username, String content) {
        User user = userService.findByUsername(username);
        if (user == null) {
            throw BusinessException.badRequest("用户不存在");
        }

        if (user.getStatus() == null || user.getStatus() == 0) {
            throw BusinessException.badRequest("账号未被封禁，无需申诉");
        }

        QueryWrapper<Appeal> query = new QueryWrapper<>();
        query.eq("user_id", user.getId());
        query.eq("status", 0);
        Long pendingCount = count(query);
        if (pendingCount > 0) {
            throw BusinessException.badRequest("您已有待审核的申诉，请耐心等待");
        }

        QueryWrapper<Appeal> banQuery = new QueryWrapper<>();
        banQuery.eq("user_id", user.getId());
        banQuery.ge("created_time", user.getBanTime());
        Long appealCount = count(banQuery);
        if (appealCount > 0) {
            throw BusinessException.badRequest("同一封禁记录只能提交1次申诉");
        }

        Appeal appeal = new Appeal();
        appeal.setUserId(user.getId());
        appeal.setContent(content);
        appeal.setStatus(0);
        save(appeal);
    }

    @Override
    @Transactional
    public void auditAppeal(Long appealId, Integer status, String auditNote, Long adminId) {
        Appeal appeal = getById(appealId);
        if (appeal == null) {
            throw BusinessException.badRequest("申诉记录不存在");
        }

        if (appeal.getStatus() != 0) {
            throw BusinessException.badRequest("该申诉已审核");
        }

        if (status == 2 && (auditNote == null || auditNote.trim().isEmpty())) {
            throw BusinessException.badRequest("驳回申诉必须填写驳回理由");
        }

        appeal.setStatus(status);
        appeal.setAuditAdminId(adminId);
        appeal.setAuditTime(LocalDateTime.now());
        appeal.setAuditNote(auditNote);
        updateById(appeal);

        if (status == 1) {
            User user = userService.getById(appeal.getUserId());
            if (user != null) {
                userService.unbanUser(user);
                messageService.sendMessage(0L, user.getId(), "账号申诉通过",
                        "您的账号申诉已通过，已恢复正常使用。", 2);
            }
        }
    }

    @Override
    @Transactional
    public void unbanExpiredUsers() {
        QueryWrapper<User> query = new QueryWrapper<>();
        query.eq("status", 2);
        query.isNotNull("ban_end_time");
        query.le("ban_end_time", LocalDateTime.now());

        List<User> expiredUsers = userService.list(query);
        for (User user : expiredUsers) {
            userService.unbanUser(user);
            messageService.sendMessage(0L, user.getId(), "账号自动解封",
                    "您的账号临时封禁已到期，已自动解封，可正常登录使用。", 2);
        }
    }
}
