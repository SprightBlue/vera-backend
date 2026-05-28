package com.unlam.verabackend.application.usecase;

import com.unlam.verabackend.domain.model.RelationshipType;
import com.unlam.verabackend.domain.model.UserCaregiver;
import com.unlam.verabackend.domain.ports.in.CreateProtectedPersonUseCase;
import com.unlam.verabackend.domain.ports.out.UserCaregiverRepositoryPort;
import com.unlam.verabackend.infrastructure.dto.CreateProtectedPersonRequest;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CreateProtectedPersonUseCaseImpl
        implements CreateProtectedPersonUseCase {

    private final UserCaregiverRepositoryPort repositoryPort;

    public CreateProtectedPersonUseCaseImpl(
            UserCaregiverRepositoryPort repositoryPort
    ) {
        this.repositoryPort = repositoryPort;
    }

    @Override
    public void execute(
            Long userId,
            CreateProtectedPersonRequest request
    ) {

        UserCaregiver caregiver = new UserCaregiver();

        caregiver.setUserId(userId);

        caregiver.setFullName(request.getFullName());

        caregiver.setRelationshipType(
                RelationshipType.fromString(
                        request.getRelationshipType()
                )
        );

        caregiver.setPhone(request.getPhone());

        caregiver.setEmail(request.getEmail());

        caregiver.setHighRiskAlertsEnabled(
                request.getHighRiskAlertsEnabled()
        );

        caregiver.setWeeklySummaryEnabled(
                request.getWeeklySummaryEnabled()
        );

        caregiver.setNotificationSensitivity(
                request.getNotificationSensitivity()
        );

        caregiver.setCreatedAt(LocalDateTime.now());

        repositoryPort.save(caregiver);

    }

    @Override
    public List<UserCaregiver> getByUserId(Long userId) {

        return repositoryPort.findByUserId(userId);

    }

}