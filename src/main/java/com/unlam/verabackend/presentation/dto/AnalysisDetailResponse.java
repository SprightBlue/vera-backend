package com.unlam.verabackend.presentation.dto;

import com.unlam.verabackend.domain.model.Analysis;
import java.time.LocalDateTime;
import java.util.UUID;

public record AnalysisDetailResponse(
        UUID id,
        LocalDateTime createdAt,
        String title,
        String source,
        String contentSummary,
        String riskType,
        String riskLevel,
        Integer riskPercentage,
        String suspiciousPatterns,
        String recommendation
) {
    public static AnalysisDetailResponse fromDomain(Analysis domain) {
        return new AnalysisDetailResponse(
                domain.getId(),
                domain.getCreatedAt(),
                domain.getTitle(),
                domain.getSource() != null ? domain.getSource().name() : null,
                domain.getContentSummary(),
                domain.getRiskType() != null ? domain.getRiskType().name() : null,
                domain.getRiskLevel() != null ? domain.getRiskLevel().name() : null,
                domain.getRiskPercentage(),
                domain.getSuspiciousPatterns(),
                domain.getRecommendation()
        );
    }
}