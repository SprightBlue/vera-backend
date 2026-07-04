package com.unlam.verabackend.infrastructure.mapper;

import com.unlam.verabackend.domain.model.ScenarioOption;
import com.unlam.verabackend.domain.model.TrainingScenario;
import com.unlam.verabackend.domain.model.TrainingSession;
import com.unlam.verabackend.infrastructure.entity.ScenarioOptionEntity;
import com.unlam.verabackend.infrastructure.entity.TrainingScenarioEntity;
import com.unlam.verabackend.infrastructure.entity.TrainingSessionEntity;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
public class TrainingMapper {

    public TrainingScenario toDomain(TrainingScenarioEntity entity) {
        return TrainingScenario.builder()
                .id(entity.getId())
                .title(entity.getTitle())
                .scenarioType(entity.getScenarioType())
                .difficulty(entity.getDifficulty())
                .senderName(entity.getSenderName())
                .senderContact(entity.getSenderContact())
                .messageBody(entity.getMessageBody())
                .scam(entity.isScam())
                .active(entity.isActive())
                .options(entity.getOptions().stream().map(this::toDomain).toList())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    public ScenarioOption toDomain(ScenarioOptionEntity entity) {
        return ScenarioOption.builder()
                .id(entity.getId())
                .label(entity.getLabel())
                .correct(entity.isCorrect())
                .feedback(entity.getFeedback())
                .warningSignals(entity.getWarningSignals())
                .displayOrder(entity.getDisplayOrder())
                .build();
    }

    public TrainingSession toDomain(TrainingSessionEntity entity) {
        return TrainingSession.builder()
                .id(entity.getId())
                .user(entity.getUser())
                .scenario(toDomain(entity.getScenario()))
                .selectedOption(entity.getSelectedOption() != null ? toDomain(entity.getSelectedOption()) : null)
                .correct(entity.getCorrect() != null && entity.getCorrect())
                .completedAt(entity.getCompletedAt())
                .assignedBy(entity.getAssignedBy())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    public List<TrainingScenario> toDomainList(List<TrainingScenarioEntity> entities) {
        return entities.stream().map(this::toDomain).toList();
    }
}