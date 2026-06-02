package com.unlam.verabackend.domain.ports.in;

import com.unlam.verabackend.domain.model.RiskAlert;

import java.util.List;

public interface ManageRiskAlertUseCase {
    List<RiskAlert> getActiveAlertsByCaregiverEmail(String email);
    RiskAlert getAlertById(String alertId);
    void markAlertAsSolved(String alertId);
}
