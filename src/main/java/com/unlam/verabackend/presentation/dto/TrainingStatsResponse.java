package com.unlam.verabackend.presentation.dto;

public record TrainingStatsResponse(
        long completed,
        long correct,
        long incorrect,
        int correctRate,
        long detectedScams
) {}