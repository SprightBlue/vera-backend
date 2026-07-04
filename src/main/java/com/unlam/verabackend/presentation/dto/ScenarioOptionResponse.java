package com.unlam.verabackend.presentation.dto;

import com.unlam.verabackend.domain.model.ScenarioOption;
import java.util.UUID;

public record ScenarioOptionResponse(
        UUID id,
        String label,
        int displayOrder
) {
    public static ScenarioOptionResponse fromDomain(ScenarioOption o) {
        return new ScenarioOptionResponse(o.getId(), o.getLabel(), o.getDisplayOrder());
    }
}