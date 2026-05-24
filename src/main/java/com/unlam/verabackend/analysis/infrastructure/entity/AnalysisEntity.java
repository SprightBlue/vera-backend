package com.unlam.verabackend.analysis.infrastructure.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Setter
@Getter
@Entity
@Table(name = "analyses")
public class AnalysisEntity {

    @Id
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(name = "message_id", nullable = false, columnDefinition = "uuid")
    private UUID messageId;

    @Column(name = "is_threat", nullable = false)
    private boolean isThreat;

    @Column(name = "risk_level_id", length = 50, nullable = false)
    private String riskLevelId;

    @Column(name = "suspicious_patterns", columnDefinition = "text", nullable = false)
    private String suspiciousPatterns;

    @Column(name = "recommendation", columnDefinition = "text", nullable = false)
    private String recommendation;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public AnalysisEntity() {
    }

    public AnalysisEntity(UUID id, UUID messageId, boolean isThreat, String riskLevelId, String suspiciousPatterns, String recommendation, LocalDateTime createdAt) {
        this.id = id;
        this.messageId = messageId;
        this.isThreat = isThreat;
        this.riskLevelId = riskLevelId;
        this.suspiciousPatterns = suspiciousPatterns;
        this.recommendation = recommendation;
        this.createdAt = createdAt;
    }

}
