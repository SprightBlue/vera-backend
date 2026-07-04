package com.unlam.verabackend.infrastructure.entity;

import com.unlam.verabackend.domain.model.TrainingDifficulty;
import com.unlam.verabackend.domain.model.TrainingScenarioType;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "training_scenarios")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TrainingScenarioEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(name = "scenario_type", nullable = false, length = 50)
    private TrainingScenarioType scenarioType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TrainingDifficulty difficulty;

    @Column(name = "sender_name")
    private String senderName;

    @Column(name = "sender_contact")
    private String senderContact;

    @Column(name = "message_body", columnDefinition = "TEXT", nullable = false)
    private String messageBody;

    @Column(name = "is_scam", nullable = false)
    private boolean scam;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    @OneToMany(mappedBy = "scenario", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @OrderBy("displayOrder ASC")
    @Builder.Default
    private List<ScenarioOptionEntity> options = new ArrayList<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}