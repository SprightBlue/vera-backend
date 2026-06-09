package com.unlam.verabackend.infrastructure.mapper;

import com.unlam.verabackend.domain.model.Alerts;
import com.unlam.verabackend.domain.model.RiskLevel;
import com.unlam.verabackend.domain.model.RiskType;
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
                .riskLevel(domain.getRiskLevel() != null ? domain.getRiskLevel().name() : null)
                .riskType(domain.getRiskType() != null ? domain.getRiskType().name() : null)
                .riskPercentage(domain.getRiskPercentage())
                .suspiciousPatterns(domain.getSuspiciousPatterns())
                .isResolved(domain.isResolved())
                .createdAt(domain.getCreatedAt())
                .resolvedAt(domain.getResolvedAt())
                .build();
    }

    public Alerts toDomain(AlertsEntity entity) {
        if (entity == null) return null;

        RiskLevel riskLevel = entity.getRiskLevel() != null
                ? RiskLevel.valueOf(entity.getRiskLevel().toUpperCase().strip())
                : null;

        RiskType riskType = entity.getRiskType() != null
                ? RiskType.valueOf(entity.getRiskType().toUpperCase().strip())
                : RiskType.NONE;

        TrustContact domainContact = null;
        if (entity.getTrustContact() != null) {
            domainContact = TrustContact.builder()
                    .id(entity.getTrustContact().getId())
                    .build();
        }

        return Alerts.builder()
                .id(entity.getId())
                .trustContact(domainContact)
                .title(entity.getTitle())
                .source(entity.getSource())
                .contentSummary(entity.getContentSummary())
                .riskLevel(riskLevel)
                .riskType(riskType)
                .riskPercentage(entity.getRiskPercentage())
                .suspiciousPatterns(entity.getSuspiciousPatterns())
                .isResolved(entity.isResolved())
                .createdAt(entity.getCreatedAt())
                .resolvedAt(entity.getResolvedAt())
                .build();
    }
}