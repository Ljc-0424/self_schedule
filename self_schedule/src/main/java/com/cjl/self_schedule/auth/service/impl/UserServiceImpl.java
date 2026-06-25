package com.cjl.self_schedule.auth.service.impl;

import com.cjl.self_schedule.auth.entity.User;
import com.cjl.self_schedule.auth.mapper.UserMapper;
import com.cjl.self_schedule.auth.service.UserService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Override
    public User findByUsername(String username) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", username);
        return getOne(queryWrapper);
    }

    @Override
    public User findByEmail(String email) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("email", email);
        return getOne(queryWrapper);
    }

    @Override
    public User findByUsernameOrEmail(String account) {
        User user = findByUsername(account);
        if (user != null) {
            return user;
        }
        return findByEmail(account);
    }

    @Override
    public boolean existsByUsername(String username) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", username);
        return count(queryWrapper) > 0;
    }

    @Override
    public boolean existsByEmail(String email) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("email", email);
        return count(queryWrapper) > 0;
    }

    @Override
    public void unbanUser(User user) {
        user.setStatus(0);
        user.setBanReason(null);
        user.setBanEndTime(null);
        user.setBanOperator(null);
        user.setBanTime(null);
        user.setIsActive(1);
        updateById(user);
    }

    @Override
    public boolean checkAndUnbanExpired(User user) {
        if (user.getStatus() != null && user.getStatus() == 2
                && user.getBanEndTime() != null
                && user.getBanEndTime().isBefore(LocalDateTime.now())) {
            unbanUser(user);
            return true;
        }
        return false;
    }
}