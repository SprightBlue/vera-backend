package com.unlam.verabackend.analysis.presentation.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
public class AnalysisResultDto {
    private UUID id;
    private UUID messageId;
    private boolean threat;
    private String riskLevel;
    private String suspiciousPatterns;
    private String recommendation;
    private LocalDateTime createdAt;
}
