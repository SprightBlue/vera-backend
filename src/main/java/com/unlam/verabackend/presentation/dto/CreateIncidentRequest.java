package com.unlam.verabackend.presentation.dto;

import com.unlam.verabackend.domain.model.IncidentActionType;
import com.unlam.verabackend.domain.model.SharedDataType;
import java.util.List;

public record CreateIncidentRequest(
        IncidentActionType actionType,
        List<SharedDataType> sharedDataTypes,
        String description
) {}