package com.unlam.verabackend.presentation.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.unlam.verabackend.domain.model.Alerts;
import com.unlam.verabackend.domain.model.Role;
import com.unlam.verabackend.presentation.utils.DateFormatter;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AlertsResponse {
    private UUID id;
    private String createdAt;
    private String title;
    private String contentSummary;

    @JsonProperty("isResolved")
    private boolean isResolved;
    private String riskLevel;

    private String protectedFullName;
    private String carerFullName;

    public static AlertsResponse fromDomain(Alerts domain, Role viewerRole) {
        if (domain == null) return null;

        String protectedName = null;
        String carerName = null;

        if (domain.getTrustContact() != null) {
            if (viewerRole == Role.CARER && domain.getTrustContact().getProtectedUser() != null) {
                protectedName = domain.getTrustContact().getProtectedUser().getFullName();
            }
            else if (viewerRole == Role.PROTECTED && domain.getTrustContact().getCarer() != null) {
                carerName = domain.getTrustContact().getCarer().getFullName();
            }
        }

        return AlertsResponse.builder()
                .id(domain.getId())
                .createdAt(DateFormatter.formatRelativeDate(domain.getCreatedAt()))
                .title(domain.getTitle())
                .contentSummary(domain.getContentSummary())
                .isResolved(domain.isResolved())
                .riskLevel(domain.getRiskLevel() != null ? domain.getRiskLevel().name() : null)
                .protectedFullName(protectedName)
                .carerFullName(carerName)
                .build();
    }
}