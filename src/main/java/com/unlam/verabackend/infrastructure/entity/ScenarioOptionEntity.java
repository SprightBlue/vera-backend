package com.unlam.verabackend.infrastructure.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;

@Entity
@Table(name = "scenario_options")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScenarioOptionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "scenario_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private TrainingScenarioEntity scenario;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String label;

    @Column(name = "is_correct", nullable = false)
    private boolean correct;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String feedback;

    @Column(name = "warning_signals", columnDefinition = "TEXT")
    private String warningSignals;

    @Column(name = "display_order", nullable = false)
    private int displayOrder;
}