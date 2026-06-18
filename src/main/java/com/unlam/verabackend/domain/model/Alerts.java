package com.unlam.verabackend.domain.model;

import java.time.LocalDateTime;
import java.util.UUID;

import com.unlam.verabackend.infrastructure.entity.TrustContact;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Alerts {
    private UUID id;
    private TrustContact trustContact;
    private String title;
    private Source source;
    private String contentSummary;
    private RiskLevel riskLevel;
    private RiskType riskType;
    private Integer riskPercentage;
    private String suspiciousPatterns;
    private boolean isResolved;
    private LocalDateTime createdAt;
    private LocalDateTime resolvedAt;
}