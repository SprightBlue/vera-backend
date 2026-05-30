package com.unlam.verabackend.presentation.dto;

public record AlertResponseDTO(
    String id,
    String title,
    String description,
    String riskLevel,
    String source,
    String timestamp
) {}