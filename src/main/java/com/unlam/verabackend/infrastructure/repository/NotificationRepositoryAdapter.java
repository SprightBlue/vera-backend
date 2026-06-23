package com.unlam.verabackend.infrastructure.repository;

import com.unlam.verabackend.domain.model.Notifications;
import com.unlam.verabackend.domain.port.out.NotificationsRepository;
import com.unlam.verabackend.infrastructure.entity.NotificationsEntity;
import com.unlam.verabackend.infrastructure.entity.User;
import com.unlam.verabackend.infrastructure.mapper.NotificationMapper;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    public Page<Notifications> findByUserEmailCreatedAtDesc(String email, Pageable pageable) {
        return jpaRepository.findByUserEmail(email, pageable)
                .map(mapper::toDomain);
    }

    @Override
    public Notifications save(Notifications notification) {
        User userEntity = entityManager.createQuery("SELECT u FROM User u WHERE u.email = :email", User.class)
                .setParameter("email", notification.getUser().getEmail())
                .getSingleResult();

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
    public List<Notifications> findUnreadByUserEmail(String email) {
        return jpaRepository.findByUserEmailAndIsReadFalse(email)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    @Transactional
    public void markAllAsReadByUserEmail(String email) {
        List<NotificationsEntity> unreadNotifications = jpaRepository.findByUserEmailAndIsReadFalse(email);

        unreadNotifications.forEach(notification -> notification.setRead(true));

        jpaRepository.saveAll(unreadNotifications);
    }
}