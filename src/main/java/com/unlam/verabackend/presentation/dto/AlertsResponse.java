package com.unlam.verabackend.presentation.dto;

import com.unlam.verabackend.domain.model.Alerts;
import java.time.LocalDateTime;
import java.util.UUID;

public record AlertsResponse(
        UUID id,
        LocalDateTime createdAt,
        String protectedFullName,
        String title,
        String contentSummary,
        boolean isResolved
) {
    public static AlertsResponse fromDomain(Alerts domain) {
        String fullName = (domain.getTrustContact() != null && domain.getTrustContact().getProtectedUser() != null)
                ? domain.getTrustContact().getProtectedUser().getFullName() : null;

        return new AlertsResponse(
                domain.getId(),
                domain.getCreatedAt(),
                fullName,
                domain.getTitle(),
                domain.getContentSummary(),
                domain.isResolved()
        );
    }
}