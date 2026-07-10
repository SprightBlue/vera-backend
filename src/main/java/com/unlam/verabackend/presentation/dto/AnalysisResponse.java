package com.unlam.verabackend.presentation.dto;

import com.unlam.verabackend.domain.model.Analysis;
import com.unlam.verabackend.presentation.utils.DateFormatter;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import java.util.UUID;

@Data
@Builder
@Schema(description = "DTO simplificado para el listado e histórico de análisis realizados")
public class AnalysisResponse {

    @Schema(description = "Identificador único del análisis (UUID)", example = "123e4567-e89b-12d3-a456-426614174000", requiredMode = Schema.RequiredMode.REQUIRED)
    private UUID id;

    @Schema(description = "Fecha de creación formateada en tiempo relativo", example = "Hace 5 minutos", requiredMode = Schema.RequiredMode.REQUIRED)
    private String createdAt;

    @Schema(description = "Título descriptivo del análisis", example = "Análisis preventivo de mensaje de texto", requiredMode = Schema.RequiredMode.REQUIRED)
    private String title;

    @Schema(description = "Resumen del contenido evaluado (fragmento inicial del texto, enlace o multimedia)", example = "Haga clic aquí para reclamar su premio...", requiredMode = Schema.RequiredMode.REQUIRED)
    private String contentSummary;

    @Schema(description = "Nivel de riesgo crítico determinado por el motor", example = "HIGH", allowableValues = {"LOW", "MEDIUM", "HIGH"}, requiredMode = Schema.RequiredMode.REQUIRED)
    private String riskLevel;

    public static AnalysisResponse fromDomain(Analysis domain) {
        if (domain == null) return null;

        return AnalysisResponse.builder()
                .id(domain.getId())
                .createdAt(DateFormatter.formatRelativeDate(domain.getCreatedAt()))
                .title(domain.getTitle())
                .contentSummary(domain.getContentSummary())
                .riskLevel(domain.getRiskLevel() != null ? domain.getRiskLevel().name() : null)
                .build();
    }
}