package com.unlam.verabackend.presentation.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record AlertDetailPresentation(
        UUID alertId,
        UUID analysisId,
        String messageContent,
        String messageSource,
        String riskLevel,
        String riskLevelDisplayName,
        String suspiciousPatterns,
        String recommendation,
        boolean received,
        LocalDateTime createdAt
) {}