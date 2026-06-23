package com.unlam.verabackend.domain.port.out;

public record GeminiResult(
        String title,
        String contentSummary,
        String riskLevel,
        String riskType,
        Integer riskPercentage,
        String suspiciousPatterns,
        String recommendation
) {}