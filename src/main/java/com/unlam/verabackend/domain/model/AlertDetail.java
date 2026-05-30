package com.unlam.verabackend.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class AlertDetail {
    private UUID alertId;
    private UUID analysisId;
    private String messageContent;
    private MessageSource messageSource;
    private RiskLevel riskLevel;
    private String suspiciousPatterns;
    private String recommendation;
    private boolean solved;
    private LocalDateTime createdAt;
}
