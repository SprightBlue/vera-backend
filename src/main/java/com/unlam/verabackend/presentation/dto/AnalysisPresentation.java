package com.unlam.verabackend.presentation.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record AnalysisPresentation(
        UUID id,
        UUID messageId,
        String riskLevel,
        String suspiciousPatterns,
        String recommendation,
        LocalDateTime createdAt
) {}
