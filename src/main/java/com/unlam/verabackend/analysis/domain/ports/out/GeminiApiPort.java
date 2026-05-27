package com.unlam.verabackend.analysis.domain.ports.out;

import com.unlam.verabackend.analysis.infrastructure.dto.GeminiDto;

public interface GeminiApiPort {
    GeminiDto analyzeMessage(String prompt);
}
