package com.unlam.verabackend.presentation.dto;

import com.unlam.verabackend.domain.model.IncidentStep;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class IncidentStepResponse {
    private UUID id;
    private String stepKey;
    private String title;
    private String description;
    private int stepOrder;
    private boolean priority;
    private boolean completed;
    private LocalDateTime completedAt;

    public static IncidentStepResponse fromDomain(IncidentStep step) {
        return IncidentStepResponse.builder()
                .id(step.getId())
                .stepKey(step.getStepKey().name())
                .title(step.getTitle())
                .description(step.getDescription())
                .stepOrder(step.getStepOrder())
                .priority(step.isPriority())
                .completed(step.isCompleted())
                .completedAt(step.getCompletedAt())
                .build();
    }
}