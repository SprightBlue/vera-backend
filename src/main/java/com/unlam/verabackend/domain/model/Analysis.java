package com.unlam.verabackend.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Analysis {
    private UUID id;
    private UUID messageId;
    private RiskLevel riskLevel;
    private String suspiciousPatterns;
    private String recommendation;
    private LocalDateTime createdAt;

    public RiskLevel getRiskLevel() {
        return this.riskLevel != null ? this.riskLevel : RiskLevel.UNDEFINED;
    }
}
