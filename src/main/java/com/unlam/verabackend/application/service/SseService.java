package com.unlam.verabackend.application.service;

import com.unlam.verabackend.domain.model.Notifications;
import com.unlam.verabackend.domain.model.NotificationsType;
import com.unlam.verabackend.infrastructure.entity.User;
import com.unlam.verabackend.domain.port.out.NotificationsRepository;
import com.unlam.verabackend.infrastructure.provider.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class SseService {

    private final NotificationsRepository repository;
    private final EmailService emailService;
    private final Map<String, SseEmitter> userEmitters = new ConcurrentHashMap<>();

    public SseEmitter createEmitter(String email) {
        log.info("Creando nuevo canal SSE (SseEmitter) para el usuario: {}", email);
        SseEmitter emitter = new SseEmitter(30 * 60 * 1000L);

        userEmitters.put(email, emitter);

        setupEmitterCallbacks(email, emitter);
        sendInitEvent(email, emitter);

        return emitter;
    }

    public void sendDeleteEvent(String email, UUID notificationId) {
        log.debug("Preparando evento NOTIFICATION_DELETED para el usuario: {}, ID de notificación: {}", email, notificationId);

        int unreadCount = repository.findUnreadByUserEmail(email).size();

        sendEventToUser(email, "NOTIFICATION_DELETED", Map.of(
                "id", notificationId,
                "unreadCount", unreadCount,
                "hasUnread", unreadCount > 0
        ));
    }

    public void sendUnreadCountUpdate(String email) {
        log.debug("Enviando actualización forzada del contador de no leídas para: {}", email);
        int unreadCount = repository.findUnreadByUserEmail(email).size();
        sendEventToUser(email, "UNREAD_COUNT_UPDATE", Map.of(
                "unreadCount", unreadCount,
                "hasUnread", unreadCount > 0
        ));
    }

    @Transactional
    public void createAndSendNotification(User targetUser, NotificationsType type, String triggeringUserFullName, Map<String, Object> payload) {
        log.info("Generando notificación de tipo {} para el usuario: {}", type, targetUser.getEmail());

        Notifications notification = buildNotification(targetUser, type, triggeringUserFullName, payload);
        Notifications saved = repository.save(notification);

        int unreadCount = repository.findUnreadByUserEmail(targetUser.getEmail()).size();

        sendEventToUser(targetUser.getEmail(), "NEW_NOTIFICATION", Map.of(
                "notification", saved,
                "unreadCount", unreadCount,
                "hasUnread", true
        ));

        sendAlertEmailIfNeeded(targetUser, type, triggeringUserFullName, payload);
    }

    private void setupEmitterCallbacks(String email, SseEmitter emitter) {
        emitter.onCompletion(() -> {
            log.debug("Canal SSE completado para el usuario: {}", email);
            userEmitters.remove(email);
        });

        emitter.onTimeout(() -> {
            log.warn("Timeout alcanzado en el canal SSE del usuario: {}", email);
            emitter.complete();
            userEmitters.remove(email);
        });

        emitter.onError((ex) -> {
            log.error("Error en el canal SSE del usuario: {}", email, ex);
            userEmitters.remove(email);
        });
    }

    private void sendInitEvent(String email, SseEmitter emitter) {
        try {
            int unreadCount = repository.findUnreadByUserEmail(email).size();
            emitter.send(SseEmitter.event().name("INIT").data(Map.of(
                    "message", "Conectado al canal de notificaciones",
                    "unreadCount", unreadCount,
                    "hasUnread", unreadCount > 0
            )));
            log.debug("Evento INIT enviado exitosamente a {} con {} pendientes.", email, unreadCount);
        } catch (IOException e) {
            log.warn("Fallo al enviar evento INIT a {}. Eliminando canal. Razón: {}", email, e.getMessage());
            userEmitters.remove(email);
        }
    }

    private void sendEventToUser(String email, String eventName, Object data) {
        SseEmitter emitter = userEmitters.get(email);
        if (emitter != null) {
            try {
                emitter.send(SseEmitter.event().name(eventName).data(data));
                log.debug("Evento '{}' enviado con éxito al usuario: {}", eventName, email);
            } catch (IOException e) {
                log.warn("Fallo de conexión al intentar enviar evento '{}' a {}. El cliente se desconectó.", eventName, email);
                userEmitters.remove(email);
            }
        } else {
            log.debug("No se pudo enviar el evento '{}' porque el usuario {} no tiene un canal SSE activo.", eventName, email);
        }
    }

    private Notifications buildNotification(User targetUser, NotificationsType type, String triggeringUserFullName, Map<String, Object> payload) {
        return Notifications.builder()
                .user(targetUser)
                .type(type)
                .title(buildTitle(type))
                .message(buildMessage(type, triggeringUserFullName))
                .payload(payload)
                .isRead(false)
                .createdAt(LocalDateTime.now())
                .build();
    }

    private void sendAlertEmailIfNeeded(User targetUser, NotificationsType type, String triggeringUserFullName, Map<String, Object> payload) {
        if (NotificationsType.ALERT.equals(type)) {
            String detalle = extractAlertDetails(payload);
            log.info("Disparando envío de correo de alerta al usuario: {}", targetUser.getEmail());
            emailService.enviarEmailAlertaRiesgoAlto(targetUser.getEmail(), triggeringUserFullName, detalle);
        }
    }

    private String extractAlertDetails(Map<String, Object> payload) {
        if (payload != null && payload.containsKey("details")) return payload.get("details").toString();
        return "Se detectó actividad sospechosa que requiere tu revisión inmediata.";
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