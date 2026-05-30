package com.unlam.verabackend.infrastructure.repository;

import com.unlam.verabackend.domain.model.RiskAlert;
import java.util.List;
import java.util.Optional;

public interface RiskAlertRepository {
    List<RiskAlert> findActiveByCaregiver(Long caregiverId);
    Optional<RiskAlert> findById(String id);
    void save(RiskAlert riskAlert);
}
