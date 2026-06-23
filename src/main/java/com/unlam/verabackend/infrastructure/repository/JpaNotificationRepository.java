package com.unlam.verabackend.infrastructure.repository;

import com.unlam.verabackend.infrastructure.entity.NotificationsEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface JpaNotificationRepository extends JpaRepository<NotificationsEntity, UUID> {
    Page<NotificationsEntity> findByUserEmail(String email, Pageable pageable);
    List<NotificationsEntity> findByUserEmailAndIsReadFalse(String email);
}