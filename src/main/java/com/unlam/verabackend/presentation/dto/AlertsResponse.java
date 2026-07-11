package com.unlam.verabackend.presentation.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.unlam.verabackend.domain.model.Alerts;
import com.unlam.verabackend.domain.model.Role;
import com.unlam.verabackend.presentation.utils.DateFormatter;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "DTO simplificado para el listado de alertas de riesgo críticas detectadas en el sistema")
public class AlertsResponse {

    @Schema(description = "Identificador único de la alerta (UUID)", example = "4a2b1c3d-5e6f-7a8b-9c0d-1e2f3a4b5c6d", requiredMode = Schema.RequiredMode.REQUIRED)
    private UUID id;

    @Schema(description = "Fecha de generación de la alerta en formato relativo", example = "Hace 10 minutos", requiredMode = Schema.RequiredMode.REQUIRED)
    private String createdAt;

    @Schema(description = "Título de la amenaza detectada", example = "Intento de Phishing Crítico Detectado", requiredMode = Schema.RequiredMode.REQUIRED)
    private String title;

    @Schema(description = "Fragmento o sumario del contenido que gatilló la alerta", example = "Mensaje SMS: 'Inicie sesión de inmediato en...' ", requiredMode = Schema.RequiredMode.REQUIRED)
    private String contentSummary;

    @JsonProperty("isResolved")
    @Schema(description = "Estado de resolución de la alerta por parte del cuidador", example = "false", requiredMode = Schema.RequiredMode.REQUIRED)
    private boolean isResolved;

    @Schema(description = "Severidad del riesgo asociado a la alerta", example = "HIGH", allowableValues = {"LOW", "MEDIUM", "HIGH"}, requiredMode = Schema.RequiredMode.REQUIRED)
    private String riskLevel;

    @Schema(description = "Nombre completo del usuario protegido afectado. Solo visible para el rol CARER.")
    private String protectedFullName;

    @Schema(description = "Nombre completo del cuidador asignado. Solo visible para el rol PROTECTED.")
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