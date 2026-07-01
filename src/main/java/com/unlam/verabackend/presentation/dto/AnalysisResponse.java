package com.unlam.verabackend.presentation.dto;

import com.unlam.verabackend.domain.model.Analysis;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

import static java.time.temporal.ChronoUnit.DAYS;

@Data
@Builder
public class AnalysisResponse {
    private UUID id;
    private String createdAt;
    private String title;
    private String contentSummary;
    private String riskLevel;

    public static AnalysisResponse fromDomain(Analysis domain) {
        if (domain == null) return null;
        return AnalysisResponse.builder()
                .id(domain.getId())
                .createdAt(formatDate(domain.getCreatedAt()))
                .title(domain.getTitle())
                .contentSummary(domain.getContentSummary())
                .riskLevel(domain.getRiskLevel() != null ? domain.getRiskLevel().name() : null)
                .build();
    }

    private static String formatDate(LocalDateTime date) {
        long days = DAYS.between(date, LocalDateTime.now());
        if (days == 0) return "Hoy";
        if (days == 1) return "Hace 1 día";
        return "Hace " + days + " días";
    }
}