package com.unlam.verabackend.presentation.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.unlam.verabackend.domain.model.UserLocation;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserLocationMapResponse {
    private String id;
    private Long trustContactId;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String locationText;

    @JsonProperty("isConnected")
    private boolean isConnected;
    private LocalDateTime updatedAt;

    public static UserLocationMapResponse fromDomain(UserLocation domain) {
        if (domain == null) return null;

        return UserLocationMapResponse.builder()
                .id(domain.getId() != null ? domain.getId().toString() : null)
                .trustContactId(domain.getTrustContact() != null ? domain.getTrustContact().getId() : null)
                .latitude(domain.getLatitude())
                .longitude(domain.getLongitude())
                .locationText(domain.getLocationText())
                .isConnected(domain.isConnected())
                .updatedAt(domain.getUpdatedAt())
                .build();
    }
}