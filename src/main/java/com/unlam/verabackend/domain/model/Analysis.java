package com.unlam.verabackend.domain.model;

import com.unlam.verabackend.infrastructure.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Analysis {
    private UUID id;
    private User user;
    private LocalDateTime createdAt;
    private String title;
    private Source source;
    private String contentSummary;
    private RiskType riskType;
    private RiskLevel riskLevel;
    private Integer riskPercentage;
    private String suspiciousPatterns;
    private String recommendation;
}