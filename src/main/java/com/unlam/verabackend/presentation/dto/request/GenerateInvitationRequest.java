package com.unlam.verabackend.presentation.dto.request;

import com.unlam.verabackend.infrastructure.entity.SensitivityLevel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class GenerateInvitationRequest {
    
    @NotBlank(message = "El nombre completo es obligatorio")
    private String fullName; 

    private String contactInfo; 

    @NotBlank(message = "La relación es obligatoria")
    private String relationship;

    @NotNull(message = "El nivel de sensibilidad es obligatorio")
    private SensitivityLevel sensitivityLevel;

    private boolean monitorWhatsapp;
    private boolean monitorSms;
    private boolean monitorGmail;
    private boolean monitorTelegram;
    private boolean notifyHighRisk;
    private boolean receiveAlertSummaries;
    private boolean allowBasicConfig;
}
