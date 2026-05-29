package com.unlam.verabackend.domain.ports.out;

import com.unlam.verabackend.domain.model.UrlValidation;
import com.unlam.verabackend.domain.model.MessageAssessment;

public interface GeminiApiPort {
    MessageAssessment analyzeMessageContent(String content, UrlValidation urlValidation);
}
