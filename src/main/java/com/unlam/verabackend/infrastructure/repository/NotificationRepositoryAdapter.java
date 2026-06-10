package com.unlam.verabackend.infrastructure.repository;

import com.unlam.verabackend.domain.model.Notifications;
import com.unlam.verabackend.domain.port.out.NotificationsRepository;
import com.unlam.verabackend.infrastructure.entity.NotificationsEntity;
import com.unlam.verabackend.infrastructure.mapper.NotificationMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class NotificationRepositoryAdapter implements NotificationsRepository {

    private final JpaNotificationRepository jpaRepository;
    private final NotificationMapper mapper;
    private final UserRepository userRepository;

    @Override
    public Page<Notifications> findByUserEmailCreatedAtDesc(String email, Pageable pageable) {
        return jpaRepository.findByUserEmail(email, pageable)
                .map(mapper::toDomain);
    }

    @Override
    public Notifications save(Notifications notification) {
        var userEntity = userRepository.findByEmail(notification.getUser().getEmail())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado para persistir notificación"));

        NotificationsEntity entity = mapper.toEntity(notification, userEntity);
        return mapper.toDomain(jpaRepository.save(entity));
    }

    @Override
    public Optional<Notifications> findById(UUID id) {
        return jpaRepository.findById(id)
                .map(mapper::toDomain);
    }

    @Override
    public void deleteById(UUID id) {
        if (!jpaRepository.existsById(id)) {
            throw new IllegalArgumentException("No se puede eliminar. Notificación no encontrada con ID: " + id);
        }
        jpaRepository.deleteById(id);
    }

    @Override
    public void markAllAsRead(String email) {
        jpaRepository.markAllAsReadByUserEmail(email);
    }
}