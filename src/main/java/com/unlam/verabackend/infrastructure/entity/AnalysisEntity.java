package com.unlam.verabackend.infrastructure.entity;

import com.unlam.verabackend.domain.model.RiskLevel;
import com.unlam.verabackend.domain.model.RiskType;
import com.unlam.verabackend.domain.model.Source;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "analysis")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnalysisEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "title", length = 255)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(name = "source", length = 255)
    private Source source;

    @Column(name = "content_summary", columnDefinition = "TEXT")
    private String contentSummary;

    @Enumerated(EnumType.STRING)
    @Column(name = "risk_type", length = 50)
    private RiskType riskType;

    @Enumerated(EnumType.STRING)
    @Column(name = "risk_level", length = 50)
    private RiskLevel riskLevel;

    @Column(name = "risk_percentage")
    private Integer riskPercentage;

    @Column(name = "suspicious_patterns", columnDefinition = "TEXT")
    private String suspiciousPatterns;

    @Column(name = "recommendation", columnDefinition = "TEXT")
    private String recommendation;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}