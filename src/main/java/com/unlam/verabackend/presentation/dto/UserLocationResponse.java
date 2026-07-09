package com.unlam.verabackend.presentation.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.unlam.verabackend.domain.model.UserLocation;
import com.unlam.verabackend.presentation.utils.DateFormatter;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class UserLocationResponse {
    private UUID id;
    private String protectedFullName;
    private String locationText;

    @JsonProperty("isConnected")
    private boolean isConnected;
    private String updatedAt;

    public static UserLocationResponse fromDomain(UserLocation domain) {
        if (domain == null) return null;

        String fullName = (domain.getTrustContact() != null && domain.getTrustContact().getProtectedUser() != null)
                ? domain.getTrustContact().getProtectedUser().getFullName() : null;

        return UserLocationResponse.builder()
                .id(domain.getId())
                .protectedFullName(fullName)
                .locationText(domain.getLocationText())
                .isConnected(domain.isConnected())
                .updatedAt(DateFormatter.formatRelativeDate(domain.getUpdatedAt()))
                .build();
    }
}