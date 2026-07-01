package com.unlam.verabackend.presentation.dto;

import com.unlam.verabackend.domain.model.Analysis;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

import static java.time.temporal.ChronoUnit.DAYS;

@Data
@Builder
public class AnalysisDetailResponse {
    private UUID id;
    private String createdAt;
    private String title;
    private String source;
    private String contentSummary;
    private String riskType;
    private String riskLevel;
    private Integer riskPercentage;
    private String suspiciousPatterns;
    private String recommendation;

    public static AnalysisDetailResponse fromDomain(Analysis domain) {
        if (domain == null) return null;

        return AnalysisDetailResponse.builder()
                .id(domain.getId())
                .createdAt(formatDate(domain.getCreatedAt()))
                .title(domain.getTitle())
                .source(domain.getSource() != null ? domain.getSource().name() : null)
                .contentSummary(domain.getContentSummary())
                .riskType(domain.getRiskType() != null ? domain.getRiskType().getSpanish() : null)
                .riskLevel(domain.getRiskLevel() != null ? domain.getRiskLevel().name() : null)
                .riskPercentage(domain.getRiskPercentage())
                .suspiciousPatterns(domain.getSuspiciousPatterns())
                .recommendation(domain.getRecommendation())
                .build();
    }

    private static String formatDate(LocalDateTime date) {
        long days = DAYS.between(date, LocalDateTime.now());
        if (days == 0) return "Hoy";
        if (days == 1) return "Hace 1 día";
        return "Hace " + days + " días";
    }
}