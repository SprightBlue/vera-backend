package com.unlam.verabackend.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Analysis {
    private UUID id;
    private DomainUser user;
    private String content;
    private MessageSource messageSource;
    private RiskLevel riskLevel;
    private String suspiciousPatterns;
    private String recommendation;
    private LocalDateTime createdAt;

    public static Analysis create(DomainUser user, String content, MessageSource source, String riskLevel, String patterns, String recommendation) {
        return new Analysis(
                UUID.randomUUID(),
                user,
                content,
                source,
                RiskLevel.fromString(riskLevel),
                patterns,
                recommendation,
                LocalDateTime.now()
        );
    }
}
