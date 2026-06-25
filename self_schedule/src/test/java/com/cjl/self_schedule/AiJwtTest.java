package com.cjl.self_schedule;

import org.junit.jupiter.api.Test;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Map;

public class AiJwtTest {

    private static final String API_KEY = "b0101d9f9a5b4200a8caea9a29243244.qY5TFpNDruMd0TeI";
    private static final String API_URL = "https://open.bigmodel.cn/api/paas/v4/chat/completions";

    /**
     * 测试智谱JWT令牌生成（使用Java标准库，绕过JJWT密钥长度限制）
     */
    @Test
    public void testJwtGeneration() {
        try {
            // 拆分API Key：格式为 id.secret
            String[] parts = API_KEY.split("\\.");
            if (parts.length != 2) {
                System.out.println("API Key格式错误！必须是 {id}.{secret} 格式");
                return;
            }
            String apiId = parts[0];
            String apiSecret = parts[1];

            System.out.println("API ID: " + apiId);
            System.out.println("API Secret: " + apiSecret);
            System.out.println("API Secret长度: " + apiSecret.length() + " 字符 (" + (apiSecret.length() * 8) + " bits)");

            // 准备JWT头
            String header = "{\"alg\":\"HS256\",\"sign_type\":\"SIGN\"}";
            String encodedHeader = base64UrlEncode(header.getBytes(StandardCharsets.UTF_8));
            System.out.println("\nHeader: " + header);
            System.out.println("Encoded Header: " + encodedHeader);

            // 准备JWT payload
            long now = System.currentTimeMillis();
            String payload = String.format(
                    "{\"api_key\":\"%s\",\"exp\":%d,\"timestamp\":%d}",
                    apiId,
                    now + 3600 * 1000, // 1小时有效期
                    now
            );
            String encodedPayload = base64UrlEncode(payload.getBytes(StandardCharsets.UTF_8));
            System.out.println("\nPayload: " + payload);
            System.out.println("Encoded Payload: " + encodedPayload);

            // 生成签名
            String data = encodedHeader + "." + encodedPayload;
            String signature = hmacSha256(data, apiSecret);
            System.out.println("\nSignature: " + signature);

            // 组合JWT令牌
            String token = encodedHeader + "." + encodedPayload + "." + signature;
            System.out.println("\n生成的JWT令牌: " + token);
            System.out.println("令牌长度: " + token.length());

        } catch (Exception e) {
            System.out.println("JWT生成错误: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 测试完整API调用
     */
    @Test
    public void testApiCall() {
        try {
            // 1. 生成JWT令牌
            String[] parts = API_KEY.split("\\.");
            String apiId = parts[0];
            String apiSecret = parts[1];

            String header = "{\"alg\":\"HS256\",\"sign_type\":\"SIGN\"}";
            String encodedHeader = base64UrlEncode(header.getBytes(StandardCharsets.UTF_8));

            long now = System.currentTimeMillis();
            String payload = String.format(
                    "{\"api_key\":\"%s\",\"exp\":%d,\"timestamp\":%d}",
                    apiId,
                    now + 3600 * 1000,
                    now
            );
            String encodedPayload = base64UrlEncode(payload.getBytes(StandardCharsets.UTF_8));

            String data = encodedHeader + "." + encodedPayload;
            String signature = hmacSha256(data, apiSecret);

            String token = encodedHeader + "." + encodedPayload + "." + signature;
            System.out.println("生成的JWT令牌: " + token);

            // 2. 构建请求
            RestTemplate restTemplate = new RestTemplate();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + token);

            Map<String, Object> systemMessage = Map.of(
                    "role", "system",
                    "content", "你是一个智能助手。"
            );

            Map<String, Object> userMessage = Map.of(
                    "role", "user",
                    "content", "你好"
            );

            Map<String, Object> requestBody = Map.of(
                    "model", "glm-4.7-flash",
                    "messages", List.of(systemMessage, userMessage),
                    "temperature", 0.1,
                    "stream", false,
                    "max_tokens", 1024
            );

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

            System.out.println("\n=== 开始测试API调用 ===");
            long startTime = System.currentTimeMillis();
            ResponseEntity<String> response = restTemplate.postForEntity(API_URL, request, String.class);
            long endTime = System.currentTimeMillis();

            System.out.println("耗时: " + (endTime - startTime) + "ms");
            System.out.println("状态码: " + response.getStatusCode());
            System.out.println("响应体: " + response.getBody());

        } catch (Exception e) {
            System.out.println("API调用错误: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Base64 URL安全编码（无填充）
     */
    private String base64UrlEncode(byte[] data) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(data);
    }

    /**
     * HMAC-SHA256签名
     */
    private String hmacSha256(String data, String secret) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKeySpec = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        mac.init(secretKeySpec);
        byte[] signatureBytes = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        return base64UrlEncode(signatureBytes);
    }
}
