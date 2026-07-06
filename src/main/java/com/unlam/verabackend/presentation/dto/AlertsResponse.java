package com.unlam.verabackend.presentation.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.unlam.verabackend.domain.model.Alerts;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class AlertsResponse {
    private UUID id;
    private String createdAt;
    private String protectedFullName;
    private String title;
    private String contentSummary;

    @JsonProperty("isResolved")
    private boolean isResolved;
    private String riskLevel;

    public static AlertsResponse fromDomain(Alerts domain) {
        if (domain == null) return null;

        String fullName = (domain.getTrustContact() != null && domain.getTrustContact().getProtectedUser() != null)
                ? domain.getTrustContact().getProtectedUser().getFullName() : null;

        return AlertsResponse.builder()
                .id(domain.getId())
                .createdAt(DateFormatter.formatRelativeDate(domain.getCreatedAt()))
                .protectedFullName(fullName)
                .title(domain.getTitle())
                .contentSummary(domain.getContentSummary())
                .isResolved(domain.isResolved())
                .riskLevel(domain.getRiskLevel() != null ? domain.getRiskLevel().name() : null)
                .build();
    }
}