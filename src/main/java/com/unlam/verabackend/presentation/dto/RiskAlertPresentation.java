package com.unlam.verabackend.presentation.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record RiskAlertPresentation(
        UUID id,
        UUID analysisId,
        Long caregiverId,
        boolean received,
        LocalDateTime createdAt
) {}
