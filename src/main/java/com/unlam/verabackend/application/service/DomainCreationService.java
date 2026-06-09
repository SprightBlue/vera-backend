package com.unlam.verabackend.application.service;

import com.unlam.verabackend.domain.model.Alerts;
import com.unlam.verabackend.domain.model.Analysis;
import com.unlam.verabackend.domain.model.RiskLevel;
import com.unlam.verabackend.domain.model.RiskType;
import com.unlam.verabackend.domain.model.Source;
import com.unlam.verabackend.domain.port.out.GeminiResult;
import com.unlam.verabackend.infrastructure.entity.User;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class DomainCreationService {

    public Analysis buildAnalysis(GeminiResult aiResult, User user, Source source) {
        RiskLevel riskLevel = aiResult.riskLevel() != null
                ? RiskLevel.valueOf(aiResult.riskLevel().toUpperCase().strip())
                : null;

        RiskType riskType = aiResult.riskType() != null
                ? RiskType.valueOf(aiResult.riskType().toUpperCase().strip())
                : RiskType.NONE;

        return Analysis.builder()
                .title(aiResult.title())
                .contentSummary(aiResult.contentSummary())
                .riskLevel(riskLevel)
                .riskType(riskType)
                .riskPercentage(aiResult.riskPercentage())
                .suspiciousPatterns(aiResult.suspiciousPatterns())
                .recommendation(aiResult.recommendation())
                .user(user)
                .createdAt(LocalDateTime.now())
                .source(source)
                .build();
    }

    // 🚀 Ahora recibe el objeto Analysis completo para desnormalizar sus datos en la Alerta
    public Alerts buildAlert(Analysis analysis) {
        return Alerts.builder()
                .title(analysis.getTitle())
                .source(analysis.getSource() != null ? analysis.getSource().name() : null)
                .contentSummary(analysis.getContentSummary())
                .riskLevel(analysis.getRiskLevel())
                .riskType(analysis.getRiskType())
                .riskPercentage(analysis.getRiskPercentage())
                .suspiciousPatterns(analysis.getSuspiciousPatterns())
                .isResolved(false)
                .createdAt(LocalDateTime.now())
                .build();
    }
}