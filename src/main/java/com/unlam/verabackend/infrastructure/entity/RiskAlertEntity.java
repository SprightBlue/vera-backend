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
@Table(name = "risk_alerts")
public class RiskAlertEntity {

    @Id
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "analysis_id", nullable = false, foreignKey = @ForeignKey(name = "fk_alerts_analyses"))
    private AnalysisEntity analysis;

    @Column(name = "caregiver_id", nullable = false)
    private Long caregiverId;

    @Column(name = "is_received", nullable = false)
    private boolean received;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

}
