package com.unlam.verabackend.domain.port.out;

import com.unlam.verabackend.domain.model.Notifications;
import org.springframework.data.domain.Page;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface NotificationsRepository {
    Page<Notifications> findByUserEmailCreatedAtDesc(String email, int page);
    Notifications save(Notifications notification);
    Optional<Notifications> findById(UUID id);
    void deleteById(UUID id);
    List<Notifications> findUnreadByUserEmail(String email);
    void markAllAsReadByUserEmail(String email);
}