package com.unlam.verabackend.domain.port.in;

import com.unlam.verabackend.domain.model.Notifications;
import org.springframework.data.domain.Page;
import java.util.UUID;

public interface ManageNotificationsUseCase {
    Page<Notifications> getMyNotifications(String email, int page);
    void markAllMyNotificationsAsRead(String email);
    void deleteNotification(UUID id, String email);
}