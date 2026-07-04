package com.unlam.verabackend.presentation.dto;

import com.unlam.verabackend.domain.model.TrainingScenario;
import java.util.List;
import java.util.UUID;

public record TrainingScenarioResponse(
        UUID id,
        String title,
        String scenarioType,
        String difficulty,
        String senderName,
        String senderContact,
        String messageBody,
        List<ScenarioOptionResponse> options
) {
    public static TrainingScenarioResponse fromDomain(TrainingScenario s) {
        return new TrainingScenarioResponse(
                s.getId(), s.getTitle(),
                s.getScenarioType().name(), s.getDifficulty().name(),
                s.getSenderName(), s.getSenderContact(), s.getMessageBody(),
                s.getOptions().stream().map(ScenarioOptionResponse::fromDomain).toList()
        );
    }
}