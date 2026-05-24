package com.unlam.verabackend.analysis.infrastructure.dto;

import com.unlam.verabackend.analysis.domain.model.RiskLevel;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class GeminiAnalysisResponse {
    private boolean isThreat;
    private RiskLevel riskLevel;
    private String suspiciousPatterns;
    private String recommendation;

    public GeminiAnalysisResponse(boolean isThreat, RiskLevel riskLevel,
                                  String suspiciousPatterns, String recommendation) {
        this.isThreat = isThreat;
        this.riskLevel = riskLevel;
        this.suspiciousPatterns = suspiciousPatterns;
        this.recommendation = recommendation;
    }

}

