package com.cjl.self_schedule.auth.controller;

import com.cjl.self_schedule.appeal.entity.Appeal;
import com.cjl.self_schedule.appeal.service.AppealService;
import com.cjl.self_schedule.appeal.vo.BanInfoVO;
import com.cjl.self_schedule.auth.dto.UserLoginDTO;
import com.cjl.self_schedule.auth.dto.UserRegisterDTO;
import com.cjl.self_schedule.auth.entity.User;
import com.cjl.self_schedule.auth.service.UserService;
import com.cjl.self_schedule.auth.vo.LoginVO;
import com.cjl.self_schedule.common.converter.VOConverter;
import com.cjl.self_schedule.common.exception.BusinessException;
import com.cjl.self_schedule.common.utils.JwtUtil;
import com.cjl.self_schedule.common.utils.PasswordUtil;
import com.cjl.self_schedule.common.vo.Result;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@Api(tags = "认证管理")
public class AuthController {

    private static final String LOGIN_ATTEMPT_PREFIX = "login:attempt:";
    private static final int MAX_LOGIN_ATTEMPTS = 5;
    private static final long LOGIN_LOCK_MINUTES = 15;

    @Autowired
    private UserService userService;

    @Autowired
    private AppealService appealService;

    @Autowired
    private VOConverter voConverter;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Value("${wechat.appid:}")
    private String wechatAppId;

    @Value("${wechat.secret:}")
    private String wechatSecret;

    @PostMapping("/register")
    @ApiOperation(value = "用户注册", notes = "创建新用户账户，默认账户状态为正常，自动生成创建时间、更新时间")
    public Result<LoginVO> register(@ApiParam(value = "注册信息", required = true) @Valid @RequestBody UserRegisterDTO registerDTO) {
        String username = registerDTO.getUsername().trim();
        String password = registerDTO.getPassword();
        String email = registerDTO.getEmail().trim();

        if (userService.existsByUsername(username)) {
            throw BusinessException.badRequest("用户名已存在");
        }

        if (userService.existsByEmail(email)) {
            throw BusinessException.badRequest("邮箱已被使用");
        }

        User user = new User();
        user.setUsername(username);
        user.setPasswordHash(PasswordUtil.encryptPassword(password));
        user.setEmail(email);
        user.setNickname("");
        user.setAvatarUrl("");
        user.setSettings(null);
        user.setIsActive(1);
        user.setStatus(0);
        user.setRole(0);
        user.setLastLoginTime(LocalDateTime.now());

        if (userService.save(user)) {
            log.info("用户注册成功: {}", username);
            String token = JwtUtil.generateToken(user.getId(), user.getUsername());
            LoginVO loginVO = voConverter.convertToLoginVO(user, token);
            return Result.success(loginVO);
        } else {
            throw BusinessException.internalError("注册失败");
        }
    }

    @PostMapping("/login")
    @ApiOperation(value = "用户登录", notes = "验证用户身份，生成JWT令牌，更新最后登录时间")
    public Result<LoginVO> login(@ApiParam(value = "登录信息", required = true) @Valid @RequestBody UserLoginDTO loginDTO) {
        String account = loginDTO.getAccount();

        // 检查是否因登录失败次数过多被锁定
        if (isLoginLocked(account)) {
            throw new BusinessException(429, "登录失败次数过多，请" + LOGIN_LOCK_MINUTES + "分钟后重试");
        }

        User user = userService.findByUsernameOrEmail(account);

        if (user == null) {
            incrementLoginAttempt(account);
            throw BusinessException.badRequest("用户不存在");
        }

        if (!PasswordUtil.verifyPassword(loginDTO.getPassword(), user.getPasswordHash())) {
            incrementLoginAttempt(account);
            throw BusinessException.badRequest("密码错误");
        }

        // 登录成功，清除失败计数
        redisTemplate.delete(LOGIN_ATTEMPT_PREFIX + account);

        // 检查用户状态
        checkUserStatus(user);

        user.setLastLoginTime(LocalDateTime.now());
        userService.updateById(user);

        log.info("用户登录成功: {}", user.getUsername());
        String token = JwtUtil.generateToken(user.getId(), user.getUsername());
        LoginVO loginVO = voConverter.convertToLoginVO(user, token);

        return Result.success(loginVO);
    }

    @GetMapping("/check-username")
    @ApiOperation(value = "检查用户名是否存在", notes = "用于注册时实时验证用户名是否已被使用")
    public Result<Map<String, Object>> checkUsername(@ApiParam(value = "用户名", required = true) @RequestParam String username) {
        boolean exists = userService.existsByUsername(username);
        Map<String, Object> result = new HashMap<>();
        result.put("exists", exists);
        result.put("username", username);
        return Result.success(result);
    }

    @GetMapping("/check-email")
    @ApiOperation(value = "检查邮箱是否存在", notes = "用于注册时实时验证邮箱是否已被使用")
    public Result<Map<String, Object>> checkEmail(@ApiParam(value = "邮箱", required = true) @RequestParam String email) {
        boolean exists = userService.existsByEmail(email);
        Map<String, Object> result = new HashMap<>();
        result.put("exists", exists);
        result.put("email", email);
        return Result.success(result);
    }

    @PostMapping("/logout")
    @ApiOperation(value = "用户注销", notes = "用户注销，前端清除本地token")
    public Result<Void> logout(@ApiParam(value = "请求对象", required = true) HttpServletRequest request) {
        // JWT 是无状态的，注销主要由前端清除 token
        // 这里验证 token 有效性，便于日志记录
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
            try {
                Long userId = JwtUtil.getUserIdFromToken(token);
                log.info("用户注销: userId={}", userId);
            } catch (Exception e) {
                // token 无效也允许注销（前端清除 token 即可）
                log.warn("注销时 token 无效: {}", e.getMessage());
            }
        }
        return Result.success("注销成功", null);
    }

    @PostMapping("/wechat-login")
    @ApiOperation(value = "微信登录", notes = "使用微信code登录，自动注册新用户")
    public Result<LoginVO> wechatLogin(@RequestBody JsonNode body) {
        String code = body.has("code") ? body.get("code").asText() : null;

        if (code == null || code.trim().isEmpty()) {
            throw BusinessException.badRequest("登录code不能为空");
        }

        if (wechatAppId == null || wechatAppId.isEmpty() || wechatSecret == null || wechatSecret.isEmpty()) {
            log.error("微信登录配置缺失");
            throw BusinessException.internalError("微信登录配置缺失");
        }

        try {
            // 调用微信接口换取 openid
            String url = String.format(
                "https://api.weixin.qq.com/sns/jscode2session?appid=%s&secret=%s&js_code=%s&grant_type=authorization_code",
                wechatAppId, wechatSecret, code
            );

            RestTemplate restTemplate = new RestTemplate();
            String response = restTemplate.getForObject(url, String.class);

            ObjectMapper mapper = new ObjectMapper();
            JsonNode jsonNode = mapper.readTree(response);

            if (jsonNode.has("errcode") && jsonNode.get("errcode").asInt() != 0) {
                String errMsg = jsonNode.has("errmsg") ? jsonNode.get("errmsg").asText() : "微信登录失败";
                log.warn("微信登录失败: {}", errMsg);
                throw BusinessException.badRequest("微信登录失败: " + errMsg);
            }

            String openId = jsonNode.has("openid") ? jsonNode.get("openid").asText() : null;

            if (openId == null || openId.isEmpty()) {
                throw BusinessException.internalError("获取openid失败");
            }

            // 根据 openid 查找用户
            QueryWrapper<User> query = new QueryWrapper<>();
            query.eq("open_id", openId);
            User user = userService.getOne(query);

            if (user == null) {
                // 新用户，自动注册
                user = new User();
                user.setOpenId(openId);
                user.setUsername("wx_" + openId.substring(0, 8));
                user.setNickname("微信用户");
                user.setPasswordHash(PasswordUtil.encryptPassword(UUID.randomUUID().toString()));
                user.setEmail("");
                user.setAvatarUrl("");
                user.setIsActive(1);
                user.setStatus(0);
                user.setRole(0);
                user.setLastLoginTime(LocalDateTime.now());
                userService.save(user);
                log.info("微信用户自动注册: {}", user.getUsername());
            } else {
                // 检查用户状态
                checkUserStatus(user);
                user.setLastLoginTime(LocalDateTime.now());
                userService.updateById(user);
            }

            String token = JwtUtil.generateToken(user.getId(), user.getUsername());
            LoginVO loginVO = voConverter.convertToLoginVO(user, token);
            return Result.success(loginVO);

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("微信登录异常", e);
            throw BusinessException.internalError("微信登录失败，请稍后重试");
        }
    }

    /**
     * 检查账号是否因登录失败次数过多被锁定
     */
    private boolean isLoginLocked(String account) {
        String attemptKey = LOGIN_ATTEMPT_PREFIX + account;
        String attemptStr = redisTemplate.opsForValue().get(attemptKey);
        if (attemptStr == null) {
            return false;
        }
        try {
            return Integer.parseInt(attemptStr) >= MAX_LOGIN_ATTEMPTS;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * 增加登录失败尝试次数，超过阈值后锁定
     */
    private void incrementLoginAttempt(String account) {
        String attemptKey = LOGIN_ATTEMPT_PREFIX + account;
        Long attempts = redisTemplate.opsForValue().increment(attemptKey);
        if (attempts != null && attempts == 1) {
            // 首次失败，设置过期时间
            redisTemplate.expire(attemptKey, LOGIN_LOCK_MINUTES, TimeUnit.MINUTES);
        }
    }

    /**
     * 检查用户状态（封禁等）
     */
    private void checkUserStatus(User user) {
        if (user.getStatus() == null || user.getStatus() == 0) {
            return;
        }

        // 检查临时封禁是否已过期
        if (userService.checkAndUnbanExpired(user)) {
            return;
        }

        // 构建封禁信息
        BanInfoVO banInfo = new BanInfoVO();
        banInfo.setStatus(user.getStatus());
        banInfo.setBanReason(user.getBanReason());
        banInfo.setBanTime(user.getBanTime());
        banInfo.setBanEndTime(user.getBanEndTime());
        banInfo.setUsername(user.getUsername());

        QueryWrapper<Appeal> appealQuery = new QueryWrapper<>();
        appealQuery.eq("user_id", user.getId());
        appealQuery.orderByDesc("created_time");
        appealQuery.last("LIMIT 1");
        Appeal latestAppeal = appealService.getOne(appealQuery);

        if (latestAppeal != null) {
            banInfo.setAppealStatus(latestAppeal.getStatus());
            banInfo.setAuditNote(latestAppeal.getAuditNote());
        } else {
            banInfo.setAppealStatus(-1);
        }

        throw new BusinessException(403, "账号已被封禁");
    }
}