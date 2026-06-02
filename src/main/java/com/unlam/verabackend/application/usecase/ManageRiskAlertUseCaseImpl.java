package com.unlam.verabackend.application.usecase;

import com.unlam.verabackend.domain.model.RiskAlert;
import com.unlam.verabackend.domain.ports.in.ManageRiskAlertUseCase;
import com.unlam.verabackend.domain.ports.out.RiskAlertRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ManageRiskAlertUseCaseImpl implements ManageRiskAlertUseCase {

    private final RiskAlertRepository riskAlertRepository;

    public ManageRiskAlertUseCaseImpl(RiskAlertRepository riskAlertRepository) {
        this.riskAlertRepository = riskAlertRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<RiskAlert> getActiveAlertsByCaregiverEmail(String email) {
        return riskAlertRepository.findActiveByCaregiverEmail(email);
    }

    @Override
    @Transactional(readOnly = true)
    public RiskAlert getAlertById(String alertId) {
        return riskAlertRepository.findById(alertId)
                .orElseThrow(() -> new RuntimeException("No se encontró la alerta de riesgo con ID: " + alertId));
    }

    @Override
    @Transactional
    public void markAlertAsSolved(String alertId) {
        RiskAlert alert = riskAlertRepository.findById(alertId)
                .orElseThrow(() -> new RuntimeException("No se encontró la alerta de riesgo con ID: " + alertId));

        alert.markAsSolved();
        riskAlertRepository.save(alert);
    }
}
