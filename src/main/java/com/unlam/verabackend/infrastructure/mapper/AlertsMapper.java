package com.unlam.verabackend.infrastructure.mapper;

import com.unlam.verabackend.domain.model.Alerts;
import com.unlam.verabackend.infrastructure.entity.AlertsEntity;
import com.unlam.verabackend.infrastructure.entity.TrustContact;
import org.springframework.stereotype.Component;

@Component
public class AlertsMapper {

    public AlertsEntity toEntity(Alerts domain, TrustContact trustContactEntity) {
        if (domain == null) return null;

        return AlertsEntity.builder()
                .id(domain.getId())
                .trustContact(trustContactEntity)
                .title(domain.getTitle())
                .source(domain.getSource())
                .contentSummary(domain.getContentSummary())
                .riskLevel(domain.getRiskLevel())
                .riskType(domain.getRiskType() != null ? domain.getRiskType() : null)
                .riskPercentage(domain.getRiskPercentage())
                .suspiciousPatterns(domain.getSuspiciousPatterns())
                .isResolved(domain.isResolved())
                .resolvedAt(domain.getResolvedAt())
                .build();
    }

    public Alerts toDomain(AlertsEntity entity) {
        if (entity == null) return null;

        return Alerts.builder()
                .id(entity.getId())
                .trustContact(entity.getTrustContact())
                .title(entity.getTitle())
                .source(entity.getSource())
                .contentSummary(entity.getContentSummary())
                .riskLevel(entity.getRiskLevel())
                .riskType(entity.getRiskType())
                .riskPercentage(entity.getRiskPercentage())
                .suspiciousPatterns(entity.getSuspiciousPatterns())
                .isResolved(entity.isResolved())
                .createdAt(entity.getCreatedAt())
                .resolvedAt(entity.getResolvedAt())
                .build();
    }
}