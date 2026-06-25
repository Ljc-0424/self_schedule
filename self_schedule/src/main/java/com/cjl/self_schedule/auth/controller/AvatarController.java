package com.cjl.self_schedule.auth.controller;

import com.cjl.self_schedule.auth.entity.User;
import com.cjl.self_schedule.auth.service.UserService;
import com.cjl.self_schedule.common.controller.BaseController;
import com.cjl.self_schedule.common.exception.BusinessException;
import com.cjl.self_schedule.common.vo.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/avatar")
@Api(tags = "头像管理")
public class AvatarController extends BaseController {

    @Autowired
    private UserService userService;

    private static final String UPLOAD_DIR = "uploads/avatars";
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB
    private static final String[] ALLOWED_EXTENSIONS = {".jpg", ".jpeg", ".png", ".gif", ".bmp", ".webp"};

    @GetMapping("/test")
    @ApiOperation(value = "测试接口", notes = "测试头像API是否正常")
    public Result<String> test() {
        return Result.success("Avatar API is working");
    }

    @PostMapping("/upload")
    @ApiOperation(value = "上传头像", notes = "上传用户头像，支持jpg、jpeg、png、gif、bmp、webp格式，最大10MB")
    public Result<Map<String, String>> uploadAvatar(
            @ApiParam(value = "请求对象", required = true) HttpServletRequest request,
            @ApiParam(value = "头像文件", required = true) @RequestParam("file") MultipartFile file) {

        Long userId = getUserIdFromRequest(request);

        if (file.isEmpty()) {
            throw BusinessException.badRequest("请选择要上传的文件");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw BusinessException.badRequest("文件大小不能超过10MB");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !isValidExtension(originalFilename)) {
            throw BusinessException.badRequest("不支持的文件格式，仅支持jpg、jpeg、png、gif、bmp、webp");
        }

        File uploadDir = new File(UPLOAD_DIR);
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }

        String extension = getFileExtension(originalFilename);
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String newFilename = userId + "_" + timestamp + "_" + UUID.randomUUID().toString().substring(0, 8) + extension;

        try {
            Path filePath = Paths.get(UPLOAD_DIR, newFilename);
            Files.copy(file.getInputStream(), filePath);

            String avatarUrl = "/api/avatar/" + newFilename;

            User user = userService.getById(userId);
            if (user != null) {
                if (user.getAvatarUrl() != null && !user.getAvatarUrl().isEmpty()) {
                    deleteOldAvatar(user.getAvatarUrl());
                }
                user.setAvatarUrl(avatarUrl);
                user.setUpdatedTime(LocalDateTime.now());
                userService.updateById(user);
            }

            Map<String, String> result = new HashMap<>();
            result.put("avatarUrl", avatarUrl);
            result.put("message", "上传成功");

            log.info("头像上传成功: userId={}, filename={}", userId, newFilename);
            return Result.success(result);

        } catch (IOException e) {
            log.error("头像上传失败: {}", e.getMessage(), e);
            throw BusinessException.internalError("上传失败");
        }
    }

    @GetMapping("/{filename}")
    @ApiOperation(value = "获取头像", notes = "根据文件名获取头像")
    public ResponseEntity<byte[]> getAvatar(@ApiParam(value = "头像文件名", required = true) @PathVariable String filename) {
        if (filename.contains("..") || filename.contains("/") || filename.contains("\\")) {
            return ResponseEntity.badRequest().build();
        }
        
        try {
            File uploadDirFile = new File(UPLOAD_DIR).getAbsoluteFile();
            Path filePath = new File(uploadDirFile, filename).toPath().normalize();
            if (!filePath.startsWith(uploadDirFile.toPath().normalize())) {
                return ResponseEntity.badRequest().build();
            }
            if (!Files.exists(filePath) || !Files.isReadable(filePath)) {
                return ResponseEntity.notFound().build();
            }

            byte[] imageBytes = Files.readAllBytes(filePath);
            String contentType = getContentType(filename);
            
            return ResponseEntity.ok()
                    .header("Content-Type", contentType)
                    .header("Cache-Control", "public, max-age=86400")
                    .body(imageBytes);
                    
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/delete")
    @ApiOperation(value = "删除头像", notes = "删除当前用户的头像")
    public Result<Void> deleteAvatar(@ApiParam(value = "请求对象", required = true) HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);

        User user = userService.getById(userId);
        checkExists(user, "用户");

        if (user.getAvatarUrl() != null && !user.getAvatarUrl().isEmpty()) {
            deleteOldAvatar(user.getAvatarUrl());
            user.setAvatarUrl(null);
            user.setUpdatedTime(LocalDateTime.now());
            userService.updateById(user);
        }

        log.info("头像删除成功: userId={}", userId);
        return Result.success("删除成功", null);
    }

    private boolean isValidExtension(String filename) {
        String lowerFilename = filename.toLowerCase();
        for (String ext : ALLOWED_EXTENSIONS) {
            if (lowerFilename.endsWith(ext)) {
                return true;
            }
        }
        return false;
    }

    private String getFileExtension(String filename) {
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex == -1) {
            return ".jpg";
        }
        return filename.substring(lastDotIndex).toLowerCase();
    }

    private String getContentType(String filename) {
        String lowerFilename = filename.toLowerCase();
        if (lowerFilename.endsWith(".jpg") || lowerFilename.endsWith(".jpeg")) {
            return "image/jpeg";
        } else if (lowerFilename.endsWith(".png")) {
            return "image/png";
        } else if (lowerFilename.endsWith(".gif")) {
            return "image/gif";
        } else if (lowerFilename.endsWith(".bmp")) {
            return "image/bmp";
        } else if (lowerFilename.endsWith(".webp")) {
            return "image/webp";
        }
        return "application/octet-stream";
    }

    private void deleteOldAvatar(String avatarUrl) {
        try {
            String filename = avatarUrl.substring(avatarUrl.lastIndexOf('/') + 1);
            Path filePath = Paths.get(UPLOAD_DIR, filename);
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            log.warn("删除旧头像失败: {}", e.getMessage());
        }
    }
}