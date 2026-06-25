package com.unlam.verabackend.presentation.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.unlam.verabackend.domain.model.UserLocation;
import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class UserLocationResponse {
    private UUID id;
    private String protectedUserName;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String locationText;

    @JsonProperty("isConnected")
    private boolean isConnected;
    private LocalDateTime updatedAt;

    public static UserLocationResponse fromDomain(UserLocation domain) {
        return UserLocationResponse.builder()
                .id(domain.getId())
                .protectedUserName(domain.getTrustContact().getProtectedUser().getFullName())
                .latitude(domain.getLatitude())
                .longitude(domain.getLongitude())
                .locationText(domain.getLocationText())
                .isConnected(domain.isConnected())
                .updatedAt(domain.getUpdatedAt())
                .build();
    }
}
