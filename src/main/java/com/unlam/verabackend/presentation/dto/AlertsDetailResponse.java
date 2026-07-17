package com.unlam.verabackend.presentation.dto;

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
@Schema(description = "DTO detallado que contiene el informe exhaustivo de una alerta de seguridad para su gestión")
public class AlertsDetailResponse {

    @Schema(description = "Identificador único de la alerta (UUID)", example = "4a2b1c3d-5e6f-7a8b-9c0d-1e2f3a4b5c6d", requiredMode = Schema.RequiredMode.REQUIRED)
    private UUID id;

    @Schema(description = "Fecha de disparo automático de la alerta", example = "Ayer", requiredMode = Schema.RequiredMode.REQUIRED)
    private String createdAt;

    @Schema(description = "Nombre completo del usuario protegido expuesto al riesgo", example = "María García", requiredMode = Schema.RequiredMode.REQUIRED)
    private String protectedFullName;

    @Schema(description = "Título descriptivo del vector de ataque", example = "Descarga de archivo ejecutable sospechoso", requiredMode = Schema.RequiredMode.REQUIRED)
    private String title;

    @Schema(description = "Canal desde donde el usuario recibió la amenaza", example = "MOBILE", allowableValues = {"WEB", "MOBILE"}, requiredMode = Schema.RequiredMode.REQUIRED)
    private String source;

    @Schema(description = "Cuerpo completo o metadatos del elemento analizado de manera automática", example = "Enlace: http://banco-seguro-actualizacion.com/login", requiredMode = Schema.RequiredMode.REQUIRED)
    private String contentSummary;

    @Schema(description = "Tipo específico de riesgo catalogado por el motor", example = "Ingeniería Social", requiredMode = Schema.RequiredMode.REQUIRED)
    private String riskType;

    @Schema(description = "Nivel de severidad de la alerta que requirió la notificación", example = "HIGH", allowableValues = {"LOW", "MEDIUM", "HIGH"}, requiredMode = Schema.RequiredMode.REQUIRED)
    private String riskLevel;

    @Schema(description = "Porcentaje de certeza de la amenaza calculado en el análisis original", example = "92", minimum = "0", maximum = "100", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer riskPercentage;

    @Schema(description = "Patrones e indicadores de compromiso (IoC) detectados en el contenido", example = "URL no coincide con dominio oficial de la entidad bancaria; Formulario solicita clave token.", requiredMode = Schema.RequiredMode.REQUIRED)
    private String suspiciousPatterns;

    @JsonProperty("isResolved")
    @Schema(description = "Determina si el cuidador ya tomó acciones mitigatorias sobre esta alerta", example = "true", requiredMode = Schema.RequiredMode.REQUIRED)
    private boolean isResolved;

    @Schema(description = "Fecha en formato relativo de cuándo se marcó la alerta como resuelta. Puede ser nulo o indicar estado si sigue activa.", example = "Hace 1 hora")
    private String resolvedAt;

    @Schema(description = "Detalle del contacto de confianza de donde se originó la alerta", requiredMode = Schema.RequiredMode.REQUIRED)
    private TrustContactResponse trustContact;

    public static AlertsDetailResponse fromDomain(Alerts alert) {
        if (alert == null) return null;

        String fullName = (alert.getTrustContact() != null && alert.getTrustContact().getProtectedUser() != null)
                ? alert.getTrustContact().getProtectedUser().getFullName() : null;

        TrustContactResponse contactResponse = null;
        if (alert.getTrustContact() != null) {
            contactResponse = TrustContactResponse.fromEntity(alert.getTrustContact(), Role.CARER);
        }

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
                .trustContact(contactResponse)
                .build();
    }
}