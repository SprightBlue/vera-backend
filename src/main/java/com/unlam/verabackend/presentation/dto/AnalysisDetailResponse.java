package com.unlam.verabackend.presentation.dto;

import com.unlam.verabackend.domain.model.Analysis;
import com.unlam.verabackend.presentation.utils.DateFormatter;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import java.util.UUID;

@Data
@Builder
@Schema(description = "DTO detallado que contiene el informe completo del análisis forense aplicado a un elemento")
public class AnalysisDetailResponse {

    @Schema(description = "Identificador único del análisis (UUID)", example = "123e4567-e89b-12d3-a456-426614174000", requiredMode = Schema.RequiredMode.REQUIRED)
    private UUID id;

    @Schema(description = "Fecha del análisis en formato relativo", example = "Ayer", requiredMode = Schema.RequiredMode.REQUIRED)
    private String createdAt;

    @Schema(description = "Título del informe de análisis", example = "Análisis de archivo multimedia sospechoso", requiredMode = Schema.RequiredMode.REQUIRED)
    private String title;

    @Schema(description = "Origen de la solicitud de análisis", example = "MOBILE", allowableValues = {"WEB", "MOBILE"}, requiredMode = Schema.RequiredMode.REQUIRED)
    private String source;

    @Schema(description = "Cuerpo o resumen del contenido textual, link o metadatos del multimedia analizado", example = "Archivo: urgente_actualizacion.apk", requiredMode = Schema.RequiredMode.REQUIRED)
    private String contentSummary;

    @Schema(description = "Categoría o tipo de riesgo detectado en el elemento", example = "Phishing / Ingeniería Social", requiredMode = Schema.RequiredMode.REQUIRED)
    private String riskType;

    @Schema(description = "Nivel de severidad del riesgo", example = "MEDIUM", allowableValues = {"LOW", "MEDIUM", "HIGH"}, requiredMode = Schema.RequiredMode.REQUIRED)
    private String riskLevel;

    @Schema(description = "Porcentaje matemático preciso de probabilidad de amenaza", example = "78", minimum = "0", maximum = "100", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer riskPercentage;

    @Schema(description = "Patrones sospechosos detectados por Heurística o IA (palabras clave, remitentes extraños, extensiones)", example = "Uso de palabras de urgencia ('URGENTE'), link acortado no oficial.", requiredMode = Schema.RequiredMode.REQUIRED)
    private String suspiciousPatterns;

    @Schema(description = "Recomendación preventiva inmediata dictada para el usuario protegido", example = "No descargar el archivo adjunto y bloquear al remitente de inmediato.", requiredMode = Schema.RequiredMode.REQUIRED)
    private String recommendation;

    public static AnalysisDetailResponse fromDomain(Analysis domain) {
        if (domain == null) return null;

        return AnalysisDetailResponse.builder()
                .id(domain.getId())
                .createdAt(DateFormatter.formatRelativeDate(domain.getCreatedAt()))
                .title(domain.getTitle())
                .source(domain.getSource() != null ? domain.getSource().getDisplayName() : null)
                .riskType(domain.getRiskType() != null ? domain.getRiskType().getDisplayName() : null)
                .riskLevel(domain.getRiskLevel() != null ? domain.getRiskLevel().name() : null)
                .riskPercentage(domain.getRiskPercentage())
                .suspiciousPatterns(domain.getSuspiciousPatterns())
                .recommendation(domain.getRecommendation())
                .contentSummary(domain.getContentSummary())
                .build();
    }
}