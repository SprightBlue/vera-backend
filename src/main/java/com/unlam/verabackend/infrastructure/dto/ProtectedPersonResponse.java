package com.unlam.verabackend.infrastructure.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ProtectedPersonResponse {

    private Long id;

    private String fullName;

    private String relationshipType;

    private String phone;

    private String email;

    private Boolean highRiskAlertsEnabled;

    private Boolean weeklySummaryEnabled;

    private String notificationSensitivity;

}