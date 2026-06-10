package com.unlam.verabackend.domain.port.in;

import com.unlam.verabackend.domain.model.Notifications;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.UUID;

public interface ManageNotificationsUseCase {
    Page<Notifications> getMyNotifications(String email, Pageable pageable);
    void markAllMyNotificationsAsRead(String email);
    void deleteNotification(UUID id, String email);
}