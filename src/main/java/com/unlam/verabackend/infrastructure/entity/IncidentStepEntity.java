package com.unlam.verabackend.infrastructure.entity;

import com.unlam.verabackend.domain.model.IncidentStepKey;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "incident_steps")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IncidentStepEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "incident_id", nullable = false)
    private IncidentEntity incident;

    @Enumerated(EnumType.STRING)
    @Column(name = "step_key", nullable = false, length = 50)
    private IncidentStepKey stepKey;

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "step_order", nullable = false)
    private int stepOrder;

    @Column(name = "is_priority", nullable = false)
    private boolean priority;

    @Column(name = "is_completed", nullable = false)
    private boolean completed = false;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;
}