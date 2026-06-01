package com.unlam.verabackend.infrastructure.entity;

import jakarta.persistence.*;
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
@Entity
@Table(name = "analyses")
public class AnalysisEntity {

    @Id
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_analyses_user"))
    private User user;

    @Column(name = "content", columnDefinition = "text", nullable = false)
    private String content;

    @Column(name = "source_id", length = 50, nullable = false)
    private String contentSourceId;

    @Column(name = "risk_level_id", length = 50, nullable = false)
    private String riskLevelId;

    @Column(name = "suspicious_patterns", columnDefinition = "text", nullable = false)
    private String suspiciousPatterns;

    @Column(name = "recommendation", columnDefinition = "text", nullable = false)
    private String recommendation;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
