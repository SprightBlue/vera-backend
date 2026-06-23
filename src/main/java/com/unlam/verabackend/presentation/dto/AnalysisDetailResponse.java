package com.unlam.verabackend.presentation.dto;

import com.unlam.verabackend.domain.model.Analysis;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class AnalysisDetailResponse {
    private UUID id;
    private LocalDateTime createdAt;
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
                .createdAt(domain.getCreatedAt())
                .title(domain.getTitle())
                .source(domain.getSource() != null ? domain.getSource().name() : null)
                .contentSummary(domain.getContentSummary())
                .riskType(domain.getRiskType() != null ? domain.getRiskType().name() : null)
                .riskLevel(domain.getRiskLevel() != null ? domain.getRiskLevel().name() : null)
                .riskPercentage(domain.getRiskPercentage())
                .suspiciousPatterns(domain.getSuspiciousPatterns())
                .recommendation(domain.getRecommendation())
                .build();
    }
}