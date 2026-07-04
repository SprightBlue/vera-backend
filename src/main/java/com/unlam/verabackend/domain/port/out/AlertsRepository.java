package com.unlam.verabackend.domain.port.out;

import com.unlam.verabackend.domain.model.Alerts;
import com.unlam.verabackend.domain.model.RiskLevel;
import org.springframework.data.domain.Page;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AlertsRepository {
    Alerts save(Alerts alert, Long trustContactId);
    void deleteById(UUID id);
    Optional<Alerts> findById(UUID id);
    void resolveAlert(UUID id, LocalDateTime resolvedAt);
    Page<Alerts> findByCriteria(List<Long> trustContactIds, Boolean isResolved, RiskLevel riskLevel, String search, int page);
}