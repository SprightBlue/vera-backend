package com.unlam.verabackend.application.usecase;

import com.unlam.verabackend.application.service.IncidentStepBuilderService;
import com.unlam.verabackend.domain.exception.ResourceNotFoundException;
import com.unlam.verabackend.domain.model.*;
import com.unlam.verabackend.domain.port.in.ManageIncidentsUseCase;
import com.unlam.verabackend.domain.port.out.IncidentRepository;
import com.unlam.verabackend.infrastructure.entity.TrustContact;
import com.unlam.verabackend.infrastructure.entity.User;
import com.unlam.verabackend.infrastructure.repository.TrustContactRepository;
import com.unlam.verabackend.infrastructure.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ManageIncidentsUseCaseImpl implements ManageIncidentsUseCase {

    private final IncidentRepository incidentRepository;
    private final IncidentStepBuilderService stepBuilderService;
    private final UserRepository userRepository;
    private final TrustContactRepository trustContactRepository;

    @Override
    @Transactional
    public Incident createIncident(String userEmail, IncidentActionType actionType, List<SharedDataType> sharedDataTypes, String description) {
        User user = findUserByEmail(userEmail);

        List<IncidentStep> steps = stepBuilderService.buildSteps(actionType, sharedDataTypes != null ? sharedDataTypes : List.of());

        Incident incident = Incident.builder()
                .user(user)
                .actionType(actionType)
                .sharedDataTypes(sharedDataTypes != null ? sharedDataTypes : List.of())
                .description(description)
                .status(IncidentStatus.IN_PROGRESS)
                .steps(steps)
                .createdAt(LocalDateTime.now())
                .build();

        return incidentRepository.save(incident);
    }

    @Override
    @Transactional(readOnly = true)
    public Incident getIncidentDetail(UUID incidentId, String requesterEmail) {
        Incident incident = findById(incidentId);

        boolean isOwner = incident.getUser().getEmail().equals(requesterEmail);
        boolean isCarer = isCarerOf(requesterEmail, incident.getUser().getId());

        if (!isOwner && !isCarer) {
            throw new AccessDeniedException("No tenés permisos para ver este incidente.");
        }
        return incident;
    }

    @Override
    @Transactional
    public Incident completeStep(UUID incidentId, IncidentStepKey stepKey, String userEmail) {
        Incident incident = findById(incidentId);

        if (!incident.getUser().getEmail().equals(userEmail)) {
            throw new AccessDeniedException("No tenés permisos para modificar este incidente.");
        }

        IncidentStep step = incident.getSteps().stream()
                .filter(s -> s.getStepKey() == stepKey && !s.isCompleted())
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Paso no encontrado o ya completado: " + stepKey));

        incidentRepository.completeStep(step.getId());

        Incident updated = findById(incidentId);
        if (updated.getSteps().stream().allMatch(IncidentStep::isCompleted)) {
            incidentRepository.markIncidentCompleted(incidentId);
            return findById(incidentId);
        }

        return updated;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Incident> getMyIncidents(String userEmail, Pageable pageable) {
        User user = findUserByEmail(userEmail);
        return incidentRepository.findByUserId(user.getId(), pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Incident> getIncidentsByTrustContact(Long trustContactId, String carerEmail, Pageable pageable) {
        TrustContact tc = trustContactRepository.findById(trustContactId)
                .orElseThrow(() -> new ResourceNotFoundException("Relación de confianza no encontrada con ID: " + trustContactId));

        if (!tc.getCarer().getEmail().equals(carerEmail)) {
            throw new AccessDeniedException("No tenés permisos para ver estos incidentes.");
        }

        return incidentRepository.findByUserId(tc.getProtectedUser().getId(), pageable);
    }

    private Incident findById(UUID id) {
        return incidentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Incidente no encontrado con ID: " + id));
    }

    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con email: " + email));
    }

    private boolean isCarerOf(String carerEmail, Long protectedUserId) {
        return userRepository.findByEmail(carerEmail)
                .map(carer -> trustContactRepository.existsByCarerIdAndProtectedUser_Id(carer.getId(), protectedUserId))
                .orElse(false);
    }
}