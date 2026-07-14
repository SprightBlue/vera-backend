package com.unlam.verabackend.domain.port.in;

import com.unlam.verabackend.domain.model.Alerts;
import com.unlam.verabackend.domain.model.RiskLevel;
import org.springframework.data.domain.Page;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

public interface ManageAlertsUseCase {
    @Transactional
    Page<Alerts> getAlertsHistory(String carerEmail, Boolean isResolved, RiskLevel riskLevel, String search, int page);

    @Transactional
    Alerts getAlertDetail(UUID id, String carerEmail);

    @Transactional
    void deleteAlert(UUID id, String carerEmail);

    @Transactional
    void resolveAlert(UUID id, String carerEmail);
}