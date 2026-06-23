package com.unlam.verabackend.infrastructure.repository;

import com.unlam.verabackend.domain.model.Incident;
import com.unlam.verabackend.domain.port.out.IncidentRepository;
import com.unlam.verabackend.infrastructure.mapper.IncidentMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class IncidentRepositoryAdapter implements IncidentRepository {

    private final JpaIncidentRepository incidentJpa;
    private final JpaIncidentStepRepository stepJpa;
    private final IncidentMapper mapper;

    @Override
    public Incident save(Incident incident) {
        return mapper.toDomain(incidentJpa.save(mapper.toEntity(incident)));
    }

    @Override
    public Optional<Incident> findById(UUID id) {
        return incidentJpa.findWithStepsById(id).map(mapper::toDomain);
    }

    @Override
    public Page<Incident> findByUserId(Long userId, Pageable pageable) {
        return incidentJpa.findByUserIdOrderByCreatedAtDesc(userId, pageable).map(mapper::toDomain);
    }

    @Override
    public void completeStep(UUID stepId) {
        stepJpa.markCompleted(stepId, LocalDateTime.now());
    }

    @Override
    public void markIncidentCompleted(UUID incidentId) {
        incidentJpa.markCompleted(incidentId, LocalDateTime.now());
    }
}