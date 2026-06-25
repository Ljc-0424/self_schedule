package com.cjl.self_schedule.common.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class SseService {

    private final Map<Long, SseEmitter> emitters = new ConcurrentHashMap<>();

    private static final long SSE_TIMEOUT = 30 * 60 * 1000L;

    public SseEmitter createEmitter(Long userId) {
        SseEmitter oldEmitter = emitters.get(userId);
        if (oldEmitter != null) {
            oldEmitter.complete();
        }

        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT);

        emitter.onCompletion(() -> {
            emitters.remove(userId, emitter);
            log.debug("SSE连接完成 - userId: {}", userId);
        });

        emitter.onTimeout(() -> {
            emitters.remove(userId, emitter);
            log.debug("SSE连接超时 - userId: {}", userId);
        });

        emitter.onError(e -> {
            emitters.remove(userId, emitter);
            log.debug("SSE连接错误 - userId: {}, error: {}", userId, e.getMessage());
        });

        emitters.put(userId, emitter);

        try {
            emitter.send(SseEmitter.event().name("connected").data("ok"));
        } catch (IOException e) {
            emitters.remove(userId, emitter);
        }

        log.debug("SSE连接创建 - userId: {}, 当前在线: {}", userId, emitters.size());
        return emitter;
    }

    public void sendToUser(Long userId, String eventName, Object data) {
        SseEmitter emitter = emitters.get(userId);
        if (emitter == null) {
            return;
        }

        try {
            emitter.send(SseEmitter.event().name(eventName).data(data));
        } catch (IOException e) {
            emitters.remove(userId, emitter);
            log.debug("SSE推送失败，移除连接 - userId: {}", userId);
        }
    }

    public int getOnlineCount() {
        return emitters.size();
    }
}
