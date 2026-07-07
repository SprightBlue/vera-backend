package com.unlam.verabackend.presentation.dto;

import com.unlam.verabackend.domain.model.Analysis;
import com.unlam.verabackend.presentation.utils.DateFormatter;
import lombok.Builder;
import lombok.Data;
import java.util.UUID;

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
                .createdAt(DateFormatter.formatRelativeDate(domain.getCreatedAt()))
                .title(domain.getTitle())
                .contentSummary(domain.getContentSummary())
                .riskLevel(domain.getRiskLevel() != null ? domain.getRiskLevel().name() : null)
                .build();
    }
}