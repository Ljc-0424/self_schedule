package com.cjl.self_schedule.auth.controller;

import com.cjl.self_schedule.auth.dto.UserUpdateDTO;
import com.cjl.self_schedule.auth.entity.User;
import com.cjl.self_schedule.auth.service.UserService;
import com.cjl.self_schedule.auth.vo.UserVO;
import com.cjl.self_schedule.common.controller.BaseController;
import com.cjl.self_schedule.common.converter.VOConverter;
import com.cjl.self_schedule.common.exception.BusinessException;
import com.cjl.self_schedule.common.utils.EncryptionUtil;
import com.cjl.self_schedule.common.vo.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/user")
@Api(tags = "用户管理")
public class UserController extends BaseController {

    @Autowired
    private UserService userService;

    @Autowired
    private VOConverter voConverter;

    @GetMapping("/info")
    @ApiOperation(value = "获取当前用户信息", notes = "获取当前登录用户的详细信息")
    public Result<UserVO> getCurrentUserInfo(@ApiParam(value = "请求对象", required = true) HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);

        User user = userService.getById(userId);
        checkExists(user, "用户");

        UserVO userVO = voConverter.convertToUserVO(user);
        return Result.success(userVO);
    }

    @PutMapping("/info")
    @ApiOperation(value = "更新用户信息", notes = "更新当前用户的昵称、邮箱、头像、个人标签、职业、生日、电话、性别、城市、爱好、简介等信息，自动更新更新时间")
    public Result<UserVO> updateUserInfo(@ApiParam(value = "请求对象", required = true) HttpServletRequest request,
            @ApiParam(value = "更新信息", required = true) @Valid @RequestBody UserUpdateDTO updateDTO) {
        Long userId = getUserIdFromRequest(request);

        User user = userService.getById(userId);
        checkExists(user, "用户");

        if (updateDTO.getNickname() != null) {
            user.setNickname(updateDTO.getNickname());
        }
        if (updateDTO.getEmail() != null) {
            if (!updateDTO.getEmail().equals(user.getEmail()) && userService.existsByEmail(updateDTO.getEmail())) {
                throw BusinessException.badRequest("邮箱已被使用");
            }
            user.setEmail(updateDTO.getEmail());
        }
        if (updateDTO.getAvatarUrl() != null) {
            user.setAvatarUrl(updateDTO.getAvatarUrl());
        }
        if (updateDTO.getSettings() != null) {
            user.setSettings(updateDTO.getSettings());
        }
        if (updateDTO.getOccupation() != null) {
            user.setOccupation(updateDTO.getOccupation());
        }
        if (updateDTO.getBirthday() != null) {
            user.setBirthday(updateDTO.getBirthday());
        }
        if (updateDTO.getPhone() != null) {
            user.setPhone(updateDTO.getPhone());
        }
        if (updateDTO.getGender() != null) {
            user.setGender(updateDTO.getGender());
        }
        if (updateDTO.getCity() != null) {
            user.setCity(updateDTO.getCity());
        }
        if (updateDTO.getHobbies() != null) {
            user.setHobbies(updateDTO.getHobbies());
        }
        if (updateDTO.getBio() != null) {
            user.setBio(updateDTO.getBio());
        }
        if (updateDTO.getWeChatWebhookUrl() != null) {
            user.setWeChatWebhookUrl(EncryptionUtil.encrypt(updateDTO.getWeChatWebhookUrl()));
        }
        if (updateDTO.getAiApiKey() != null) {
            user.setAiApiKey(EncryptionUtil.encrypt(updateDTO.getAiApiKey()));
        }
        if (updateDTO.getDailyFocusGoal() != null) {
            user.setDailyFocusGoal(updateDTO.getDailyFocusGoal());
        }
        if (updateDTO.getMinEffectiveDuration() != null) {
            user.setMinEffectiveDuration(updateDTO.getMinEffectiveDuration());
        }

        user.setUpdatedTime(java.time.LocalDateTime.now());

        if (userService.updateById(user)) {
            User updatedUser = userService.getById(userId);
            UserVO userVO = voConverter.convertToUserVO(updatedUser);
            log.info("用户信息更新成功: userId={}", userId);
            return Result.success(userVO);
        } else {
            throw BusinessException.internalError("更新失败");
        }
    }
}