package com.unlam.verabackend.infrastructure.repository;

import com.unlam.verabackend.domain.model.TrainingScenario;
import com.unlam.verabackend.domain.model.TrainingSession;
import com.unlam.verabackend.domain.port.out.TrainingRepository;
import com.unlam.verabackend.infrastructure.entity.ScenarioOptionEntity;
import com.unlam.verabackend.infrastructure.entity.TrainingScenarioEntity;
import com.unlam.verabackend.infrastructure.entity.TrainingSessionEntity;
import com.unlam.verabackend.infrastructure.entity.User;
import com.unlam.verabackend.infrastructure.mapper.TrainingMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class TrainingRepositoryAdapter implements TrainingRepository {

    private final JpaTrainingScenarioRepository scenarioJpa;
    private final JpaTrainingSessionRepository sessionJpa;
    private final TrainingMapper mapper;

    @Override
    public List<TrainingScenario> findActiveScenarios() {
        return mapper.toDomainList(scenarioJpa.findByActiveTrue());
    }

    @Override
    public Optional<TrainingScenario> findScenarioById(UUID id) {
        return scenarioJpa.findById(id).map(mapper::toDomain);
    }

    @Override
    public TrainingSession saveSession(TrainingSession session) {
        TrainingScenarioEntity scenarioEntity = scenarioJpa.findById(session.getScenario().getId())
                .orElseThrow();

        ScenarioOptionEntity optionEntity = null;
        if (session.getSelectedOption() != null) {
            optionEntity = scenarioEntity.getOptions().stream()
                    .filter(o -> o.getId().equals(session.getSelectedOption().getId()))
                    .findFirst()
                    .orElse(null);
        }

        TrainingSessionEntity entity = TrainingSessionEntity.builder()
                .user(session.getUser())
                .scenario(scenarioEntity)
                .selectedOption(optionEntity)
                .correct(session.isCorrect())
                .completedAt(session.getCompletedAt() != null ? session.getCompletedAt() : LocalDateTime.now())
                .assignedBy(session.getAssignedBy())
                .build();

        return mapper.toDomain(sessionJpa.save(entity));
    }

    @Override
    public Page<TrainingSession> findSessionsByUserId(Long userId, Pageable pageable) {
        return sessionJpa.findByUserIdOrderByCreatedAtDesc(userId, pageable).map(mapper::toDomain);
    }

    @Override
    public List<TrainingSession> findCompletedSessionsByUserId(Long userId) {
        return sessionJpa.findCompletedByUserId(userId).stream().map(mapper::toDomain).toList();
    }

    @Override
    public long countCompletedByUserId(Long userId) {
        return sessionJpa.countByUserIdAndCompletedAtIsNotNull(userId);
    }

    @Override
    public long countCorrectByUserId(Long userId) {
        return sessionJpa.countByUserIdAndCorrectTrue(userId);
    }
}