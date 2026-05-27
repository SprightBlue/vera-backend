package com.unlam.verabackend.analysis.presentation.dto;

public record MessagePresentation(
        Long userId,
        String content,
        String source
) {}
