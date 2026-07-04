package com.unlam.verabackend.domain.port.in;

import com.unlam.verabackend.domain.model.Alerts;
import com.unlam.verabackend.domain.model.RiskLevel;
import org.springframework.data.domain.Page;

import java.util.UUID;

public interface ManageAlertsUseCase {
    Page<Alerts> getAlertsHistory(String carerEmail, Boolean isResolved, RiskLevel riskLevel, String search, int page);
    Alerts getAlertDetail(UUID id, String carerEmail);
    void deleteAlert(UUID id, String carerEmail);
    void resolveAlert(UUID id, String carerEmail);
}