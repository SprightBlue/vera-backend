package com.unlam.verabackend.domain.port.in;

import com.unlam.verabackend.domain.model.Notifications;
import org.springframework.data.domain.Page;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

public interface ManageNotificationsUseCase {
    @Transactional
    Page<Notifications> getMyNotifications(String email, int page);

    @Transactional
    void markAllMyNotificationsAsRead(String email);

    @Transactional
    void deleteNotification(UUID id, String email);

    @Transactional
    void deleteAllMyNotifications(String email);
}