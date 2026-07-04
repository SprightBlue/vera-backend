package com.unlam.verabackend.presentation.dto;

import com.unlam.verabackend.domain.model.SensitivityLevel;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AddContactRequest(
        @NotBlank String fullName,
        String contactPhone,
        @NotBlank @Email String contactEmail,
        @NotNull String relationship,
        SensitivityLevel sensitivityLevel,
        boolean notifyHighRisk,
        boolean receiveAlertSummaries
) {}