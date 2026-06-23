package com.unlam.verabackend.presentation.dto;

import com.unlam.verabackend.domain.model.SensitivityLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CarerResponse {
    private Long contactId; 
    private String fullName;
    private String email;
    private String relationship;
    private SensitivityLevel sensitivityLevel;
    private boolean notifyHighRisk;
}