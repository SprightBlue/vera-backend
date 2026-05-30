package com.unlam.verabackend.domain.model;

public record MessageAssessment(
        String riskLevel,
        String suspiciousPatterns,
        String recommendation
) {}
