package com.unlam.verabackend.domain.port.out;

import com.unlam.verabackend.domain.model.Notifications;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.Optional;
import java.util.UUID;

public interface NotificationsRepository {
    Notifications save(Notifications notification);
    Page<Notifications> findByUserEmailCreatedAtDesc(String email, Pageable pageable);
    Optional<Notifications> findById(UUID id);
    void deleteById(UUID id);
    void markAllAsRead(String email);
}