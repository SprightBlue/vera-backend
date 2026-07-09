package com.unlam.verabackend.application.service;

import com.unlam.verabackend.domain.model.Notifications;
import com.unlam.verabackend.domain.model.NotificationsType;
import com.unlam.verabackend.domain.port.out.RtcProvider;
import com.unlam.verabackend.domain.port.out.NotificationsRepository;
import com.unlam.verabackend.infrastructure.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationsRepository notificationsRepository;
    private final RtcProvider rtcProvider;

    @Transactional
    public void createAndDispatch(User targetUser, NotificationsType type, String triggeringUserFullName, Map<String, Object> payload) {
        log.info("Application Service: Creando notificación transaccional [{}] para el usuario [{}]", type, targetUser.getEmail());

        Notifications notification = buildNotificationStructure(targetUser, type, triggeringUserFullName, payload);
        Notifications saved = notificationsRepository.save(notification);

        log.debug("Application Service: Notificación guardada ID [{}]. Recalculando contador en tiempo real...", saved.getId());
        int unreadCount = getUnreadNotificationCount(targetUser.getEmail());

        log.info("Application Service: Despachando notificación a través del bus unificado RTC para [{}]", targetUser.getEmail());
        rtcProvider.publishNewNotification(targetUser.getEmail(), saved, unreadCount);
    }

    private Notifications buildNotificationStructure(User targetUser, NotificationsType type, String name, Map<String, Object> payload) {
        return Notifications.builder()
                .user(targetUser)
                .type(type)
                .title(buildTitle(type))
                .message(buildMessage(type, name))
                .payload(payload)
                .isRead(false)
                .createdAt(LocalDateTime.now())
                .build();
    }

    private int getUnreadNotificationCount(String email) {
        return (int) notificationsRepository.countUnreadByUserEmail(email);
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
            case ALERT -> String.format("El usuario %s ha generado una alerta de riesgo crítico.", name);
            case ALERT_SOLVED -> String.format("La alerta de %s ha sido marcada como resuelta.", name);
            case INVITATION -> String.format("%s te ha enviado una solicitud para ser su contacto de confianza.", name);
            case INVITATION_ACCEPTED -> String.format("%s aceptó tu invitación de confianza.", name);
            case INVITATION_REJECTED -> String.format("%s rechazó tu invitación de confianza.", name);
        };
    }
}