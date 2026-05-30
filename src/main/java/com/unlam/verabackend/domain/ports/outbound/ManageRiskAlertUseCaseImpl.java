package com.unlam.verabackend.domain.ports.outbound;

import com.unlam.verabackend.domain.model.RiskAlert;
import com.unlam.verabackend.domain.ports.inbound.ManageRiskAlertUseCase;
import com.unlam.verabackend.domain.ports.out.LinkGeneratorService;
import com.unlam.verabackend.infrastructure.repository.RiskAlertRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ManageRiskAlertUseCaseImpl implements ManageRiskAlertUseCase {

    private final RiskAlertRepository riskAlertRepository;
    private final LinkGeneratorService linkGeneratorService;

    public ManageRiskAlertUseCaseImpl(RiskAlertRepository riskAlertRepository, LinkGeneratorService linkGeneratorService) {
        this.riskAlertRepository = riskAlertRepository;
        this.linkGeneratorService = linkGeneratorService;
    }

    @Override
    @Transactional
    public List<RiskAlert> getActiveAlertsByCaregiver(Long caregiverId) {
        return riskAlertRepository.findActiveByCaregiver(caregiverId);
    }

    @Override
    @Transactional
    public void markAlertAsSolved(String alertId) {
        RiskAlert alert = riskAlertRepository.findById(alertId)
                .orElseThrow(() -> new RuntimeException("No se encontró la alerta de riesgo con ID: " + alertId));

        alert.markAsSolved();
        riskAlertRepository.save(alert);
    }

    @Override
    @Transactional
    public String getContactLinkForUser(String alertId) {
        RiskAlert alert = riskAlertRepository.findById(alertId)
                .orElseThrow(() -> new RuntimeException("No se encontró la alerta de riesgo con ID: " + alertId));

        String targetEmail = alert.getAnalysis().getUser().getEmail();
        String subject = "Seguimiento - Alerta de Seguridad Sistema VERA";

        return linkGeneratorService.generateEmailLink(targetEmail, subject);
    }
}
