package com.cjl.self_schedule.common.controller;

import com.cjl.self_schedule.common.service.SseService;
import com.cjl.self_schedule.common.utils.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Slf4j
@RestController
@RequestMapping("/api/sse")
public class SseController {

    @Autowired
    private SseService sseService;

    @GetMapping(value = "/connect", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter connect(@RequestParam String token) {
        try {
            String t = token;
            if (t.startsWith("Bearer ")) {
                t = t.substring(7);
            }
            JwtUtil.validateToken(t);
            Long userId = JwtUtil.getUserIdFromToken(t);
            if (userId == null) {
                SseEmitter emitter = new SseEmitter(0L);
                emitter.completeWithError(new RuntimeException("无效token"));
                return emitter;
            }
            return sseService.createEmitter(userId);
        } catch (Exception e) {
            log.warn("SSE连接认证失败: {}", e.getMessage());
            SseEmitter emitter = new SseEmitter(0L);
            emitter.completeWithError(e);
            return emitter;
        }
    }
}
