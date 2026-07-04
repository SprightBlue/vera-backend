package com.unlam.verabackend.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TrainingScenario {
    private UUID id;
    private String title;
    private TrainingScenarioType scenarioType;
    private TrainingDifficulty difficulty;
    private String senderName;
    private String senderContact;
    private String messageBody;
    private boolean scam;
    private boolean active;
    private List<ScenarioOption> options;
    private LocalDateTime createdAt;
}