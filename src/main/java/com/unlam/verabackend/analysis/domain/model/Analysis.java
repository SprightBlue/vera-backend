package com.unlam.verabackend.analysis.domain.model;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Setter
@Getter
public class Analysis {
    private UUID id;
    private UUID messageId;
    private boolean isThreat;
    private RiskLevel riskLevel;
    private String suspiciousPatterns;
    private String recommendation;
    private LocalDateTime createdAt;

    public Analysis(UUID id, UUID messageId, boolean isThreat, RiskLevel riskLevel,
                    String suspiciousPatterns, String recommendation, LocalDateTime createdAt) {
        this.id = id;
        this.messageId = messageId;
        this.isThreat = isThreat;
        this.riskLevel = riskLevel;
        this.suspiciousPatterns = suspiciousPatterns;
        this.recommendation = recommendation;
        this.createdAt = createdAt;
    }

}
