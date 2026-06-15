package com.unlam.verabackend.infrastructure.mapper;

import com.unlam.verabackend.domain.model.Incident;
import com.unlam.verabackend.domain.model.IncidentStep;
import com.unlam.verabackend.infrastructure.entity.IncidentEntity;
import com.unlam.verabackend.infrastructure.entity.IncidentStepEntity;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class IncidentMapper {

    public Incident toDomain(IncidentEntity entity) {
        if (entity == null) return null;

        List<IncidentStep> steps = entity.getSteps() == null
                ? List.of()
                : entity.getSteps().stream().map(this::stepToDomain).toList();

        return Incident.builder()
                .id(entity.getId())
                .user(entity.getUser())
                .actionType(entity.getActionType())
                .sharedDataTypes(entity.getSharedDataTypes())
                .description(entity.getDescription())
                .status(entity.getStatus())
                .steps(steps)
                .createdAt(entity.getCreatedAt())
                .completedAt(entity.getCompletedAt())
                .build();
    }

    public IncidentEntity toEntity(Incident domain) {
        IncidentEntity entity = IncidentEntity.builder()
                .user(domain.getUser())
                .actionType(domain.getActionType())
                .sharedDataTypes(domain.getSharedDataTypes())
                .description(domain.getDescription())
                .status(domain.getStatus())
                .createdAt(domain.getCreatedAt())
                .build();

        entity.setSteps(domain.getSteps().stream()
                .map(step -> stepToEntity(step, entity))
                .toList());

        return entity;
    }

    private IncidentStep stepToDomain(IncidentStepEntity entity) {
        return IncidentStep.builder()
                .id(entity.getId())
                .stepKey(entity.getStepKey())
                .title(entity.getTitle())
                .description(entity.getDescription())
                .stepOrder(entity.getStepOrder())
                .priority(entity.isPriority())
                .completed(entity.isCompleted())
                .completedAt(entity.getCompletedAt())
                .build();
    }

    private IncidentStepEntity stepToEntity(IncidentStep domain, IncidentEntity incident) {
        return IncidentStepEntity.builder()
                .incident(incident)
                .stepKey(domain.getStepKey())
                .title(domain.getTitle())
                .description(domain.getDescription())
                .stepOrder(domain.getStepOrder())
                .priority(domain.isPriority())
                .completed(false)
                .build();
    }
}