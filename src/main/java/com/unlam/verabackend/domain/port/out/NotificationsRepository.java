package com.unlam.verabackend.domain.port.out;

import com.unlam.verabackend.domain.model.Notifications;
import org.springframework.data.domain.Page;
import java.util.Optional;
import java.util.UUID;

public interface NotificationsRepository {
    Notifications save(Notifications notification);
    Optional<Notifications> findById(UUID id);
    Page<Notifications> findByUserEmailCreatedAtDesc(String email, int page);
    void deleteById(UUID id);
    long countUnreadByUserEmail(String email);
    void markAllAsReadByUserEmail(String email);
    void deleteAllByUserEmail(String email);
}