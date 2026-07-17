package com.unlam.verabackend.domain.model;

public record AiResult(
        String title, String contentSummary, String riskLevel, String riskType,
        Integer riskPercentage, String suspiciousPatterns, String recommendation
) {}