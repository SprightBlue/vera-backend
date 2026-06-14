package com.unlam.verabackend.domain.model;

import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IncidentStep {
    private UUID id;
    private IncidentStepKey stepKey;
    private String title;
    private String description;
    private int stepOrder;
    private boolean priority;
    private boolean completed;
    private LocalDateTime completedAt;
}
