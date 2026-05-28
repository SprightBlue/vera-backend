package com.unlam.verabackend.infrastructure.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateProtectedPersonRequest {

    private String fullName;

    private String relationshipType;

    private String phone;

    private String email;

    private Boolean highRiskAlertsEnabled;

    private Boolean weeklySummaryEnabled;

    private String notificationSensitivity;

}