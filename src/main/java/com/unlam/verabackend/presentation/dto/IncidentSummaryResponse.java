package com.unlam.verabackend.presentation.dto;

import com.unlam.verabackend.domain.model.Incident;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class IncidentSummaryResponse {
    private UUID id;
    private String actionType;
    private String status;
    private String protectedUserName;
    private LocalDateTime createdAt;
    private int totalSteps;
    private int completedStepsCount;

    public static IncidentSummaryResponse fromDomain(Incident incident) {
        long completed = incident.getSteps().stream().filter(s -> s.isCompleted()).count();

        return IncidentSummaryResponse.builder()
                .id(incident.getId())
                .actionType(incident.getActionType().name())
                .status(incident.getStatus().name())
                .protectedUserName(incident.getUser().getFullName())
                .createdAt(incident.getCreatedAt())
                .totalSteps(incident.getSteps().size())
                .completedStepsCount((int) completed)
                .build();
    }
}