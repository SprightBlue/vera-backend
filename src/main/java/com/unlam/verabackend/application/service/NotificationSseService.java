package com.unlam.verabackend.application.service;

import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class NotificationSseService {

    private final Map<Long, SseEmitter> userEmitters = new ConcurrentHashMap<>();

    public SseEmitter createEmitter(Long userId) {
        // Timeout de 10 minutos para conexiones persistentes
        SseEmitter emitter = new SseEmitter(10 * 60 * 1000L);

        userEmitters.put(userId, emitter);

        emitter.onCompletion(() -> userEmitters.remove(userId));
        emitter.onTimeout(() -> userEmitters.remove(userId));
        emitter.onError((e) -> userEmitters.remove(userId));

        try {
            emitter.send(SseEmitter.event().name("INIT").data("Conectado al canal unificado"));
        } catch (IOException ignored) {}

        return emitter;
    }

    public void sendNotification(Long userId, String eventName, Object data) {
        SseEmitter emitter = userEmitters.get(userId);
        if (emitter != null) {
            try {
                emitter.send(SseEmitter.event().name(eventName).data(data));
            } catch (IOException e) {
                userEmitters.remove(userId);
            }
        }
    }
}