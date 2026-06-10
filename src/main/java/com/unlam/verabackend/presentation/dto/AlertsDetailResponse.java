package com.unlam.verabackend.presentation.dto;

import com.unlam.verabackend.domain.model.Alerts;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class AlertsDetailResponse {
    private UUID id;
    private LocalDateTime createdAt;
    private String protectedFullName;
    private String title;
    private String source;
    private String contentSummary;
    private String riskType;
    private String riskLevel;
    private Integer riskPercentage;
    private String suspiciousPatterns;
    private boolean isResolved;
    private LocalDateTime resolvedAt;

    public static AlertsDetailResponse fromDomain(Alerts alert) {
        if (alert == null) return null;

        String fullName = (alert.getTrustContact() != null && alert.getTrustContact().getProtectedUser() != null)
                ? alert.getTrustContact().getProtectedUser().getFullName() : null;

        return AlertsDetailResponse.builder()
                .id(alert.getId())
                .createdAt(alert.getCreatedAt())
                .protectedFullName(fullName)
                .title(alert.getTitle())
                .source(alert.getSource() != null ? alert.getSource().name() : null)
                .contentSummary(alert.getContentSummary())
                .riskType(alert.getRiskType() != null ? alert.getRiskType().name() : null)
                .riskLevel(alert.getRiskLevel() != null ? alert.getRiskLevel().name() : null)
                .riskPercentage(alert.getRiskPercentage())
                .suspiciousPatterns(alert.getSuspiciousPatterns())
                .isResolved(alert.isResolved())
                .resolvedAt(alert.getResolvedAt())
                .build();
    }
}