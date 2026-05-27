package com.unlam.verabackend.presentation.dto;

public record MessagePresentation(
        Long userId,
        String content,
        String source
) {}
