package com.unlam.verabackend.application.usecase;

import com.unlam.verabackend.application.service.SseService;
import com.unlam.verabackend.domain.exception.ResourceNotFoundException;
import com.unlam.verabackend.domain.model.Notifications;
import com.unlam.verabackend.domain.port.in.ManageNotificationsUseCase;
import com.unlam.verabackend.domain.port.out.DeviceTokenRepository;
import com.unlam.verabackend.domain.port.out.NotificationsRepository;
import com.unlam.verabackend.infrastructure.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ManageNotificationsUseCaseImpl implements ManageNotificationsUseCase {

    private final NotificationsRepository repository;
    private final DeviceTokenRepository deviceTokenRepository;
    private final SseService sseService;

    @Override
    @Transactional(readOnly = true)
    public Page<Notifications> getMyNotifications(String email, Pageable pageable) {
        return repository.findByUserEmailCreatedAtDesc(email, pageable);
    }

    @Override
    @Transactional
    public void markAllMyNotificationsAsRead(String email) {
        repository.markAllAsReadByUserEmail(email);
    }

    private Notifications getNotificationDetail(UUID id, String email) {
        Notifications notification = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("La notificación solicitada no existe."));

        if (!notification.getUser().getEmail().equals(email)) {
            throw new AccessDeniedException("No tenés permisos para interactuar con esta notificación.");
        }
        return notification;
    }

    @Override
    @Transactional
    public void deleteNotification(UUID id, String email) {
        Notifications notification = getNotificationDetail(id, email);
        repository.deleteById(notification.getId());
        sseService.sendDeleteEvent(email, id);
    }

    @Override
    @Transactional
    public void registerDeviceToken(User user, String token, String platform) {
        if (user == null) {
            throw new IllegalArgumentException("Usuario autenticado requerido.");
        }
        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException("El token del dispositivo es obligatorio.");
        }
        if (platform == null || platform.isBlank()) {
            throw new IllegalArgumentException("La plataforma del dispositivo es obligatoria.");
        }

        deviceTokenRepository.saveOrUpdate(user, token.trim(), platform.trim().toLowerCase());
    }
}
