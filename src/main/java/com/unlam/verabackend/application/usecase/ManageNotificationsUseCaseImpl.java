package com.unlam.verabackend.application.usecase;

import com.unlam.verabackend.domain.exception.ResourceNotFoundException;
import com.unlam.verabackend.domain.model.Notifications;
import com.unlam.verabackend.domain.port.in.ManageNotificationsUseCase;
import com.unlam.verabackend.domain.port.out.RtcProvider;
import com.unlam.verabackend.domain.port.out.NotificationsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ManageNotificationsUseCaseImpl implements ManageNotificationsUseCase {

    private final NotificationsRepository repository;
    private final RtcProvider rtcProvider;

    @Override
    @Transactional(readOnly = true)
    public Page<Notifications> getMyNotifications(String email, int page) {
        log.info("UseCase: Solicitando página [{}] de notificaciones indexadas para el usuario: [{}]", page, email);
        return repository.findByUserEmailCreatedAtDesc(email, page); // <-- Pasa el int
    }

    @Override
    @Transactional
    public void markAllMyNotificationsAsRead(String email) {
        log.info("UseCase: Solicitando marcado masivo de lectura para el usuario: [{}]", email);
        repository.markAllAsReadByUserEmail(email);

        int unreadCount = getUnreadNotificationCount(email);
        log.debug("UseCase: Sincronizando nuevo conteo masivo ({}) vía RTC para [{}]", unreadCount, email);
        rtcProvider.publishUnreadCountUpdate(email, unreadCount);
    }

    @Override
    @Transactional
    public void deleteNotification(UUID id, String email) {
        log.info("UseCase: Iniciando remoción de notificación ID: [{}] para el operador: [{}]", id, email);

        Notifications notification = validateAndGetOwnedNotification(id, email);
        repository.deleteById(notification.getId());

        log.info("UseCase: Registro ID: [{}] removido de la persistencia con éxito.", id);

        int unreadCount = getUnreadNotificationCount(email);
        log.debug("UseCase: Notificando eliminación y actualizando delta de contador ({}) vía RTC...", unreadCount);
        rtcProvider.publishNotificationDeleted(email, id, unreadCount);
    }

    private Notifications validateAndGetOwnedNotification(UUID id, String email) {
        log.debug("UseCase: Extrayendo detalles de notificación ID: [{}]", id);
        Notifications notification = repository.findById(id)
                .orElseThrow(() -> {
                    log.error("UseCase Error: La notificación solicitada ID: [{}] no existe.", id);
                    return new ResourceNotFoundException("La notificación solicitada no existe.");
                });

        if (!notification.getUser().getEmail().equalsIgnoreCase(email)) {
            log.warn("ALERTA DE VIOLACIÓN DE PROPIEDAD: El usuario [{}] intentó alterar la notificación privada del usuario [{}]",
                    email, notification.getUser().getEmail());
            throw new AccessDeniedException("No tenés permisos para interactuar con esta notificación.");
        }

        return notification;
    }

    private int getUnreadNotificationCount(String email) {
        return (int) repository.countUnreadByUserEmail(email);
    }
}