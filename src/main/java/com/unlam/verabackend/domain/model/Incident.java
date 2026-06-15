package com.unlam.verabackend.domain.model;

import com.unlam.verabackend.infrastructure.entity.User;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Incident {
    private UUID id;
    private User user;
    private IncidentActionType actionType;
    private List<SharedDataType> sharedDataTypes;
    private String description;
    private IncidentStatus status;
    private List<IncidentStep> steps;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;
}