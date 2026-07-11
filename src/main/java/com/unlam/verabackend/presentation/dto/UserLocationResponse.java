package com.unlam.verabackend.presentation.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.unlam.verabackend.domain.model.UserLocation;
import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "DTO que representa la telemetría de ubicación en tiempo real de un usuario protegido")
public class UserLocationResponse {

    @Schema(description = "Identificador único del registro de ubicación en la base de series temporales (ej. MongoDB ID)", example = "65f1c2d3e4b5a67890123456", requiredMode = Schema.RequiredMode.REQUIRED)
    private String id;

    @Schema(description = "ID de la relación de confianza (TrustContact) que vincula al protegido con su cuidador", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long trustContactId;

    @Schema(description = "Latitud geográfica precisa obtenida por el GPS del dispositivo móvil", example = "-34.651034", requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal latitude;

    @Schema(description = "Longitud geográfica precisa obtenida por el GPS del dispositivo móvil", example = "-58.621234", requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal longitude;

    @Schema(description = "Dirección aproximada por geocodificación inversa (Capa opcional)", example = "Florencio Varela 1903, San Justo, Buenos Aires")
    private String locationText;

    @JsonProperty("isConnected")
    @Schema(description = "Determina si el dispositivo móvil del protegido se encuentra transmitiendo en este momento", example = "true", requiredMode = Schema.RequiredMode.REQUIRED)
    private boolean isConnected;

    @Schema(description = "Fecha y hora exacta del último ping o actualización de coordenadas de red", example = "2026-07-10T10:15:30", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime updatedAt;

    public static UserLocationResponse fromDomain(UserLocation domain) {
        if (domain == null) return null;

        return UserLocationResponse.builder()
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