package com.unlam.verabackend.presentation.dto;

import com.unlam.verabackend.domain.model.Incident;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class IncidentDetailResponse {
    private UUID id;
    private String actionType;
    private List<String> sharedDataTypes;
    private String description;
    private String status;
    private String protectedUserName;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;
    private List<IncidentStepResponse> prioritySteps;
    private List<IncidentStepResponse> recommendedSteps;
    private int totalSteps;
    private int completedStepsCount;
    private int priorityStepsCount;
    private int completedPriorityStepsCount;

    public static IncidentDetailResponse fromDomain(Incident incident) {
        List<IncidentStepResponse> priority = incident.getSteps().stream()
                .filter(s -> s.isPriority())
                .map(IncidentStepResponse::fromDomain)
                .toList();

        List<IncidentStepResponse> recommended = incident.getSteps().stream()
                .filter(s -> !s.isPriority())
                .map(IncidentStepResponse::fromDomain)
                .toList();

        long completedPriority = priority.stream().filter(IncidentStepResponse::isCompleted).count();
        long completedTotal = incident.getSteps().stream().filter(s -> s.isCompleted()).count();

        return IncidentDetailResponse.builder()
                .id(incident.getId())
                .actionType(incident.getActionType().name())
                .sharedDataTypes(incident.getSharedDataTypes().stream().map(Enum::name).toList())
                .description(incident.getDescription())
                .status(incident.getStatus().name())
                .protectedUserName(incident.getUser().getFullName())
                .createdAt(incident.getCreatedAt())
                .completedAt(incident.getCompletedAt())
                .prioritySteps(priority)
                .recommendedSteps(recommended)
                .totalSteps(incident.getSteps().size())
                .completedStepsCount((int) completedTotal)
                .priorityStepsCount(priority.size())
                .completedPriorityStepsCount((int) completedPriority)
                .build();
    }
}