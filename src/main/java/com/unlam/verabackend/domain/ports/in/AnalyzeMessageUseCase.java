package com.unlam.verabackend.domain.ports.in;

import com.unlam.verabackend.domain.model.Analysis;
import com.unlam.verabackend.domain.model.MessageSource;

public interface AnalyzeMessageUseCase {
    Analysis analyzeMessage(String userEmail, String content, MessageSource source);
}
