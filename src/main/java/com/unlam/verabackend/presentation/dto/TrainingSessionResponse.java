package com.unlam.verabackend.presentation.dto;

import com.unlam.verabackend.domain.model.TrainingSession;
import java.time.LocalDateTime;
import java.util.UUID;

public record TrainingSessionResponse(
        UUID sessionId,
        UUID scenarioId,
        String scenarioTitle,
        String scenarioType,
        boolean correct,
        String selectedOptionLabel,
        String feedback,
        String warningSignals,
        LocalDateTime completedAt
) {
    public static TrainingSessionResponse fromDomain(TrainingSession s) {
        String optionLabel = s.getSelectedOption() != null ? s.getSelectedOption().getLabel() : null;
        String feedback = s.getSelectedOption() != null ? s.getSelectedOption().getFeedback() : null;
        String warnings = s.getSelectedOption() != null ? s.getSelectedOption().getWarningSignals() : null;
        return new TrainingSessionResponse(s.getId(), s.getScenario().getId(), s.getScenario().getTitle(), s.getScenario().getScenarioType().name(), s.isCorrect(), optionLabel, feedback, warnings, s.getCompletedAt());
    }
}