package com.unlam.verabackend.domain.port.out;

import com.unlam.verabackend.domain.model.Alerts;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AlertsRepository {
    Alerts save(Alerts alert, Long trustContactId);
    void deleteById(UUID id);
    Optional<Alerts> findById(UUID id);
    Page<Alerts> findByTrustContactIds(List<Long> trustContactIds, Pageable pageable);
    Page<Alerts> findByTrustContactIdsAndIsResolved(List<Long> trustContactIds, boolean isResolved, Pageable pageable);
}