package com.unlam.verabackend.presentation.dto;

import com.unlam.verabackend.domain.model.Alerts;
import java.time.LocalDateTime;
import java.util.UUID;

public record AlertsDetailResponse(
        UUID id,
        LocalDateTime createdAt,
        String protectedFullName,
        String title,
        String source,
        String contentSummary,
        String riskType,
        String riskLevel,
        Integer riskPercentage,
        String suspiciousPatterns,
        boolean isResolved,
        LocalDateTime resolvedAt
) {
    public static AlertsDetailResponse fromDomain(Alerts alert) {
        String fullName = (alert.getTrustContact() != null && alert.getTrustContact().getProtectedUser() != null)
                ? alert.getTrustContact().getProtectedUser().getFullName() : null;

        return new AlertsDetailResponse(
                alert.getId(),
                alert.getCreatedAt(),
                fullName,
                alert.getTitle(),
                alert.getSource(),
                alert.getContentSummary(),
                alert.getRiskType() != null ? alert.getRiskType().name() : null,
                alert.getRiskLevel() != null ? alert.getRiskLevel().name() : null,
                alert.getRiskPercentage(),
                alert.getSuspiciousPatterns(),
                alert.isResolved(),
                alert.getResolvedAt()
        );
    }
}