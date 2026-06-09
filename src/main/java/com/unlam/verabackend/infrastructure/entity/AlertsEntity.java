package com.unlam.verabackend.infrastructure.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "alerts", schema = "public")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AlertsEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trust_contact_id", nullable = false)
    private TrustContact trustContact;

    @Column(name = "title")
    private String title;

    @Column(name = "source")
    private String source;

    @Column(name = "content_summary", columnDefinition = "TEXT")
    private String contentSummary;

    @Column(name = "risk_level", length = 50)
    private String riskLevel;

    @Column(name = "risk_type", length = 50)
    private String riskType;

    @Column(name = "risk_percentage")
    private Integer riskPercentage;

    @Column(name = "suspicious_patterns", columnDefinition = "TEXT")
    private String suspiciousPatterns;

    @Column(name = "is_resolved", nullable = false)
    private boolean isResolved = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}