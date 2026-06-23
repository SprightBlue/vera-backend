package com.unlam.verabackend.domain.port.out;

import com.unlam.verabackend.domain.model.Incident;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.Optional;
import java.util.UUID;


public interface IncidentRepository {
    Incident save(Incident incident);
    Optional<Incident> findById(UUID id);
    Page<Incident> findByUserId(Long userId, Pageable pageable);
    void completeStep(UUID stepId);
    void markIncidentCompleted(UUID incidentId);
}
