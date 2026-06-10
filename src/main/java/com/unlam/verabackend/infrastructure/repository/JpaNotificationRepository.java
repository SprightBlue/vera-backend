package com.unlam.verabackend.infrastructure.repository;

import com.unlam.verabackend.infrastructure.entity.NotificationsEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Repository
public interface JpaNotificationRepository extends JpaRepository<NotificationsEntity, UUID> {
    Page<NotificationsEntity> findByUserEmail(String email, Pageable pageable);

    @Modifying
    @Transactional
    @Query("UPDATE NotificationsEntity n SET n.isRead = true WHERE n.user.email = :email AND n.isRead = false")
    void markAllAsReadByUserEmail(@Param("email") String email);
}