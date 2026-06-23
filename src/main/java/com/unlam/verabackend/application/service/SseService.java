package com.unlam.verabackend.application.service;

import com.unlam.verabackend.domain.model.Notifications;
import com.unlam.verabackend.domain.model.NotificationsType;
import com.unlam.verabackend.infrastructure.entity.User;
import com.unlam.verabackend.domain.port.out.NotificationsRepository;
import com.unlam.verabackend.domain.port.out.PushNotificationSender;
import com.unlam.verabackend.infrastructure.provider.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class SseService {

    private final NotificationsRepository repository;
    private final EmailService emailService;
    private final PushNotificationSender pushNotificationSender;
    private final Map<String, SseEmitter> userEmitters = new ConcurrentHashMap<>();

    public SseEmitter createEmitter(String email) {
        SseEmitter emitter = new SseEmitter(30 * 60 * 1000L);

        userEmitters.put(email, emitter);

        emitter.onCompletion(() -> userEmitters.remove(email));

        emitter.onTimeout(() -> {
            emitter.complete();
            userEmitters.remove(email);
        });

        emitter.onError((ex) -> userEmitters.remove(email));

        try {
            emitter.send(SseEmitter.event().name("INIT").data("Conectado al canal de notificaciones"));
        } catch (IOException ignored) {}

        return emitter;
    }

    public void sendDeleteEvent(String email, UUID notificationId) {
        SseEmitter emitter = userEmitters.get(email);
        if (emitter != null) {
            try {
                emitter.send(SseEmitter.event()
                        .name("NOTIFICATION_DELETED")
                        .data(Map.of("id", notificationId)));
            } catch (IOException e) {
                userEmitters.remove(email);
            }
        }
    }

    @Transactional
    public Notifications createAndSendNotification(User targetUser, NotificationsType type, String triggeringUserFullName, Map<String, Object> payload) {

        String title = buildTitle(type);
        String message = buildMessage(type, triggeringUserFullName);

        Notifications notification = Notifications.builder()
                .user(targetUser)
                .type(type)
                .title(title)
                .message(message)
                .payload(payload)
                .isRead(false)
                .createdAt(LocalDateTime.now())
                .build();

        Notifications saved = repository.save(notification);

        sendSse(targetUser.getEmail(), saved);
        sendPush(saved);

        if (type == NotificationsType.ALERT) {
            String detalle = (payload != null && payload.containsKey("details")) 
                    ? payload.get("details").toString() 
                    : "Se detectó actividad sospechosa que requiere tu revisión inmediata.";
            
            emailService.enviarEmailAlertaRiesgoAlto(targetUser.getEmail(), triggeringUserFullName, detalle);
        }

        return saved;
    }

    private void sendSse(String email, Object data) {
        SseEmitter emitter = userEmitters.get(email);
        if (emitter != null) {
            try {
                emitter.send(SseEmitter.event().name("NEW_NOTIFICATION").data(data));
            } catch (IOException e) {
                userEmitters.remove(email);
            }
        }
    }

    private void sendPush(Notifications notification) {
        try {
            pushNotificationSender.send(notification);
        } catch (RuntimeException ignored) {}
    }

    private String buildTitle(NotificationsType type) {
        return switch (type) {
            case ALERT -> "¡Alerta de Seguridad Detectada!";
            case ALERT_SOLVED -> "Alerta Resuelta";
            case INVITATION -> "Nueva Invitación de Contacto";
            case INVITATION_ACCEPTED -> "Invitación Aceptada";
            case INVITATION_REJECTED -> "Invitación Rechazada";
        };
    }

    private String buildMessage(NotificationsType type, String name) {
        return switch (type) {
            case ALERT -> "El usuario " + name + " ha generado una alerta de riesgo crítico.";
            case ALERT_SOLVED -> "La alerta de " + name + " ha sido marcada como resuelta.";
            case INVITATION -> name + " te ha enviado una solicitud para ser su contacto de confianza.";
            case INVITATION_ACCEPTED -> name + " aceptó tu invitación de confianza.";
            case INVITATION_REJECTED -> name + " rechazó tu invitación de confianza.";
        };
    }
}
