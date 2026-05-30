package com.unlam.verabackend.application.service;

import com.unlam.verabackend.infrastructure.entity.RiskAlertEntity;
import com.unlam.verabackend.infrastructure.repository.RiskAlertJpaRepository;
import com.unlam.verabackend.presentation.dto.AlertResponseDTO;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AlertService {

    private final RiskAlertJpaRepository riskAlertJpaRepository;

    public AlertService(RiskAlertJpaRepository riskAlertJpaRepository) {
        this.riskAlertJpaRepository = riskAlertJpaRepository;
    }

    public List<AlertResponseDTO> getAllAlerts() {
        List<RiskAlertEntity> riskAlerts = riskAlertJpaRepository.findAll();

        return riskAlerts.stream().map(alert -> {
           
            String id = alert.getId().toString();
            String riskLevel = alert.getAnalysis().getRiskLevelId(); 
            String source = alert.getAnalysis().getMessage().getSourceId();
            String description = alert.getAnalysis().getSuspiciousPatterns();
            String title = "Alerta Detectada"; // Título genérico o lógico
            String timestamp = alert.getCreatedAt().toString();

            return new AlertResponseDTO(id, title, description, riskLevel, source, timestamp);
        }).toList();
    }
}