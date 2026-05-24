package com.unlam.verabackend.analysis.domain.ports.out;

import com.unlam.verabackend.analysis.infrastructure.dto.GeminiAnalysisRequest;
import com.unlam.verabackend.analysis.infrastructure.dto.GeminiAnalysisResponse;

public interface GeminiAnalysisPort {
    GeminiAnalysisResponse analyzeMessage(GeminiAnalysisRequest request);
}
