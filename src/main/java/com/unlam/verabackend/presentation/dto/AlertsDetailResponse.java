package com.unlam.verabackend.presentation.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.unlam.verabackend.domain.model.Alerts;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class AlertsDetailResponse {
    private UUID id;
    private String createdAt;
    private String protectedFullName;
    private String title;
    private String source;
    private String contentSummary;
    private String riskType;
    private String riskLevel;
    private Integer riskPercentage;
    private String suspiciousPatterns;

    @JsonProperty("isResolved")
    private boolean isResolved;
    private String resolvedAt;

    public static AlertsDetailResponse fromDomain(Alerts alert) {
        if (alert == null) return null;

        String fullName = (alert.getTrustContact() != null && alert.getTrustContact().getProtectedUser() != null)
                ? alert.getTrustContact().getProtectedUser().getFullName() : null;

        return AlertsDetailResponse.builder()
                .id(alert.getId())
                .createdAt(DateFormatter.formatRelativeDate(alert.getCreatedAt()))
                .protectedFullName(fullName)
                .title(alert.getTitle())
                .source(alert.getSource() != null ? alert.getSource().getDisplayName() : null)
                .contentSummary(alert.getContentSummary())
                .riskType(alert.getRiskType() != null ? alert.getRiskType().getDisplayName() : null)
                .riskLevel(alert.getRiskLevel() != null ? alert.getRiskLevel().name() : null)
                .riskPercentage(alert.getRiskPercentage())
                .suspiciousPatterns(alert.getSuspiciousPatterns())
                .isResolved(alert.isResolved())
                .resolvedAt(DateFormatter.formatRelativeDate(alert.getResolvedAt()))
                .build();
    }
}