package com.unlam.verabackend.domain.ports.out;

import com.unlam.verabackend.domain.model.RiskAlert;
import java.util.List;
import java.util.Optional;

public interface RiskAlertRepository {
    List<RiskAlert> findActiveByCaregiver(Long caregiverId);
    List<RiskAlert> findActiveByCaregiverEmail(String email);
    Optional<RiskAlert> findById(String id);
    RiskAlert save(RiskAlert riskAlert);
}
