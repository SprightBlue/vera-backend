package com.unlam.verabackend.domain.port.in;

import com.unlam.verabackend.domain.model.TrainingScenario;
import com.unlam.verabackend.domain.model.TrainingSession;
import com.unlam.verabackend.presentation.dto.TrainingProgressResponse;
import com.unlam.verabackend.presentation.dto.TrainingStatsResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.UUID;

public interface ManageTrainingUseCase {
    List<TrainingScenario> getAvailableScenarios(String userEmail);
    TrainingScenario getScenarioById(UUID scenarioId);
    TrainingSession submitAnswer(String userEmail, UUID scenarioId, UUID selectedOptionId);
    void assignScenario(String carerEmail, Long trustContactId, UUID scenarioId);
    TrainingProgressResponse getProgressForProtected(String carerEmail, Long trustContactId);
    TrainingStatsResponse getStatsForProtected(String carerEmail, Long trustContactId);
    Page<TrainingSession> getSessionsForProtected(String carerEmail, Long trustContactId, Pageable pageable);
}