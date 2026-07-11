package com.unlam.verabackend.infrastructure.repository;

import com.unlam.verabackend.domain.model.Notifications;
import com.unlam.verabackend.domain.port.out.NotificationsRepository;
import com.unlam.verabackend.infrastructure.entity.NotificationsEntity;
import com.unlam.verabackend.infrastructure.entity.User;
import com.unlam.verabackend.infrastructure.mapper.NotificationMapper;
import com.unlam.verabackend.infrastructure.repository.jpa.JpaNotificationRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class NotificationRepositoryAdapter implements NotificationsRepository {

    private final JpaNotificationRepository jpaRepository;
    private final NotificationMapper mapper;
    private final EntityManager entityManager;

    @Override
    public Page<Notifications> findByUserEmailCreatedAtDesc(String email, int page) {
        Pageable customPageable = PageRequest.of(
                page,
                5,
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        return jpaRepository.findByUserEmail(email, customPageable)
                .map(mapper::toDomain);
    }

    @Override
    @Transactional
    public Notifications save(Notifications notification) {
        User userProxy = entityManager.getReference(User.class, notification.getUser().getId());
        NotificationsEntity entity = mapper.toEntity(notification, userProxy);
        NotificationsEntity savedEntity = jpaRepository.saveAndFlush(entity);
        return mapper.toDomain(savedEntity);
    }

    @Override
    public Optional<Notifications> findById(UUID id) {
        return jpaRepository.findById(id)
                .map(mapper::toDomain);
    }

    @Override
    @Transactional
    public void deleteById(UUID id) {
        if (!jpaRepository.existsById(id)) {
            throw new IllegalArgumentException("No se puede eliminar. Notificación no encontrada con ID: " + id);
        }
        jpaRepository.deleteById(id);
    }

    @Override
    public long countUnreadByUserEmail(String email) {
        return jpaRepository.countByUserEmailAndIsReadFalse(email);
    }

    @Override
    @Transactional
    public void markAllAsReadByUserEmail(String email) {
        List<NotificationsEntity> unreadNotifications = jpaRepository.findByUserEmailAndIsReadFalse(email);
        unreadNotifications.forEach(notification -> notification.setRead(true));
        jpaRepository.saveAllAndFlush(unreadNotifications);
    }
}