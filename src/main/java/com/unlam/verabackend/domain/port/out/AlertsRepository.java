package com.unlam.verabackend.domain.port.out;

import com.unlam.verabackend.domain.model.Alerts;
import com.unlam.verabackend.domain.model.RiskLevel;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AlertsRepository {
    Alerts save(Alerts alert, Long trustContactId);
    void deleteById(UUID id);
    Optional<Alerts> findById(UUID id);
    Page<Alerts> findByCriteria(List<Long> trustContactIds, Boolean isResolved, RiskLevel riskLevel, String search, int page);
    List<Alerts> findTop3ActiveAlertsByCarerEmail(String email);
    long countAlertsByCarerEmailInLast24Hours(String email);
    List<Alerts> findTop3ResolvedAlertsByUserEmail(String email);
    long countResolvedAlertsInLast24Hours(String email);
}