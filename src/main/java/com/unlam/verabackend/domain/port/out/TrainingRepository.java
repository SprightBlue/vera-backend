package com.unlam.verabackend.domain.port.out;

import com.unlam.verabackend.domain.model.TrainingScenario;
import com.unlam.verabackend.domain.model.TrainingSession;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TrainingRepository {
    List<TrainingScenario> findActiveScenarios();
    Optional<TrainingScenario> findScenarioById(UUID id);
    TrainingSession saveSession(TrainingSession session);
    Page<TrainingSession> findSessionsByUserId(Long userId, Pageable pageable);
    List<TrainingSession> findCompletedSessionsByUserId(Long userId);
    long countCompletedByUserId(Long userId);
    long countCorrectByUserId(Long userId);
}