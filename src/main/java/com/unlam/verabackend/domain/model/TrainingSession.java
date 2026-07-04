package com.unlam.verabackend.domain.model;

import com.unlam.verabackend.infrastructure.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TrainingSession {
    private UUID id;
    private User user;
    private TrainingScenario scenario;
    private ScenarioOption selectedOption;
    private boolean correct;
    private LocalDateTime completedAt;
    private User assignedBy;
    private LocalDateTime createdAt;
}