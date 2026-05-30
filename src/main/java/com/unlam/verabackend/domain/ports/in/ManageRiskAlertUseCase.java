package com.unlam.verabackend.domain.ports.in;

import com.unlam.verabackend.domain.model.RiskAlert;

import java.util.List;

public interface ManageRiskAlertUseCase {
    List<RiskAlert> getActiveAlertsByCaregiver(Long caregiverId);
    void markAlertAsSolved(String alertId);
    String getContactLinkForUser(String alertId);
}
