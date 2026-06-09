package com.unlam.verabackend.presentation.dto;

import com.unlam.verabackend.domain.model.Analysis;
import java.time.LocalDateTime;
import java.util.UUID;

public record AnalysisResponse(
        UUID id,
        LocalDateTime createdAt,
        String title,
        String contentSummary,
        String riskLevel
) {
    public static AnalysisResponse fromDomain(Analysis domain) {
        return new AnalysisResponse(
                domain.getId(),
                domain.getCreatedAt(),
                domain.getTitle(),
                domain.getContentSummary(),
                domain.getRiskLevel() != null ? domain.getRiskLevel().name() : null
        );
    }
}