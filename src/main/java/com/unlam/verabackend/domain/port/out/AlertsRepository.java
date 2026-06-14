package com.unlam.verabackend.domain.port.out;

import com.unlam.verabackend.domain.model.Alerts;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AlertsRepository {
    Alerts save(Alerts alert, Long trustContactId);
    void deleteById(UUID id);
    Optional<Alerts> findById(UUID id);
    Page<Alerts> findByTrustContactIdsCreatedAtDesc(List<Long> trustContactIds, Pageable pageable);
    Page<Alerts> findByTrustContactIdsAndIsResolvedCreatedAtDesc(List<Long> trustContactIds, boolean isResolved, Pageable pageable);
    void resolveAlertDirectly(UUID id, LocalDateTime resolvedAt);
}