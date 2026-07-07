package com.unlam.verabackend.application.usecase;

import com.unlam.verabackend.application.service.SseService;
import com.unlam.verabackend.domain.exception.ResourceNotFoundException;
import com.unlam.verabackend.domain.model.Notifications;
import com.unlam.verabackend.domain.port.in.ManageNotificationsUseCase;
import com.unlam.verabackend.domain.port.out.NotificationsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ManageNotificationsUseCaseImpl implements ManageNotificationsUseCase {

    private final NotificationsRepository repository;
    private final SseService sseService;

    @Override
    @Transactional(readOnly = true)
    public Page<Notifications> getMyNotifications(String email, Pageable pageable) {
        log.info("Solicitando página de notificaciones para el usuario: {}", email);
        return repository.findByUserEmailCreatedAtDesc(email, pageable);
    }

    @Override
    @Transactional
    public void markAllMyNotificationsAsRead(String email) {
        log.info("Marcando todas las notificaciones como leídas para el usuario: {}", email);
        repository.markAllAsReadByUserEmail(email);

        sseService.sendUnreadCountUpdate(email);
    }

    private Notifications getNotificationDetail(UUID id, String email) {
        log.debug("Buscando detalle de notificación ID: {} para validación de propiedad ({})", id, email);
        Notifications notification = repository.findById(id)
                .orElseThrow(() -> {
                    log.error("No se encontró la notificación con ID: {}", id);
                    return new ResourceNotFoundException("La notificación solicitada no existe.");
                });

        if (!notification.getUser().getEmail().equals(email)) {
            log.warn("Acceso denegado: El usuario {} intentó acceder a la notificación del usuario {}", email, notification.getUser().getEmail());
            throw new AccessDeniedException("No tenés permisos para interactuar con esta notificación.");
        }
        return notification;
    }

    @Override
    @Transactional
    public void deleteNotification(UUID id, String email) {
        log.info("Iniciando proceso de eliminación de notificación ID: {} por el usuario: {}", id, email);
        Notifications notification = getNotificationDetail(id, email);

        repository.deleteById(notification.getId());
        log.info("Notificación ID: {} eliminada correctamente del repositorio.", id);

        sseService.sendDeleteEvent(email, id);
    }
}