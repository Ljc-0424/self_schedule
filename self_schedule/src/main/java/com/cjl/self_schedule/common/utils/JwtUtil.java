package com.cjl.self_schedule.common.utils;

import com.cjl.self_schedule.common.exception.BusinessException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;
import javax.crypto.SecretKey;

@Slf4j
@Component
public class JwtUtil {

    /**
     * 从配置文件读取 JWT 密钥
     *
     * ⚠️ 安全警告：生产环境必须替换此密钥！
     * 密钥长度应至少 256 位（32 字节），建议使用 512 位以上。
     * 请通过环境变量 JWT_SECRET_KEY 或 .env 文件配置随机生成的强密钥。
     * 绝对不要使用默认值或硬编码的密钥部署到生产环境。
     */
    @Value("${jwt.secret-key}")
    private String secretKey;

    @Value("${jwt.expiration-time:86400000}")
    private long expirationTime;

    private static JwtUtil INSTANCE;

    @jakarta.annotation.PostConstruct
    public void init() {
        INSTANCE = this;
    }

    private SecretKey getSigningKey() {
        // 确保密钥长度至少 256 位（32 字节），不足时进行 hash 处理
        byte[] keyBytes = secretKey.getBytes();
        if (keyBytes.length < 32) {
            try {
                java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
                keyBytes = digest.digest(keyBytes);
            } catch (Exception e) {
                log.error("JWT 密钥处理失败", e);
                throw new BusinessException(500, "JWT 密钥处理失败");
            }
        }
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * 生成 JWT Token
     */
    public static String generateToken(Long userId, String username) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + INSTANCE.expirationTime);

        return Jwts.builder()
                .setSubject(userId.toString())
                .claim("username", username)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(INSTANCE.getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * 验证并解析 Token
     */
    public static Claims validateToken(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(INSTANCE.getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException e) {
            log.warn("Token 已过期: {}", e.getMessage());
            throw new BusinessException(401, "Token 已过期，请重新登录");
        } catch (MalformedJwtException e) {
            log.warn("Token 格式无效: {}", e.getMessage());
            throw new BusinessException(401, "Token 格式无效");
        } catch (SignatureException e) {
            log.warn("Token 签名验证失败: {}", e.getMessage());
            throw new BusinessException(401, "Token 签名验证失败");
        } catch (Exception e) {
            log.warn("Token 验证失败: {}", e.getMessage());
            throw new BusinessException(401, "Token 无效");
        }
    }

    /**
     * 从 Token 中获取用户 ID
     */
    public static Long getUserIdFromToken(String token) {
        Claims claims = validateToken(token);
        return Long.parseLong(claims.getSubject());
    }

    /**
     * 从 Authorization Header 中获取用户 ID
     */
    public static Long getUserIdFromAuthorizationHeader(String authorizationHeader) {
        if (authorizationHeader == null || authorizationHeader.isEmpty()) {
            throw new BusinessException(401, "缺少 Authorization header");
        }

        String token = authorizationHeader;
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        return getUserIdFromToken(token);
    }
}
