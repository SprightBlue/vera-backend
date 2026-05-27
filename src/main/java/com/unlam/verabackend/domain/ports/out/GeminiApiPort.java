package com.unlam.verabackend.domain.ports.out;

import com.unlam.verabackend.infrastructure.dto.GeminiDto;

public interface GeminiApiPort {
    GeminiDto analyzeMessage(String prompt);
}
