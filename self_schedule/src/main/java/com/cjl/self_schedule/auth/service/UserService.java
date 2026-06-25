package com.cjl.self_schedule.auth.service;

import com.cjl.self_schedule.auth.entity.User;
import com.baomidou.mybatisplus.extension.service.IService;

public interface UserService extends IService<User> {

    User findByUsername(String username);

    User findByEmail(String email);

    User findByUsernameOrEmail(String account);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    /**
     * 解封用户，清除封禁相关信息
     */
    void unbanUser(User user);

    /**
     * 检查并处理临时封禁到期的用户，返回 true 表示已自动解封
     */
    boolean checkAndUnbanExpired(User user);
}