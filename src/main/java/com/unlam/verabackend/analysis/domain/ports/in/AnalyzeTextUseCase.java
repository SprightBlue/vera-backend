package com.unlam.verabackend.analysis.domain.ports.in;

import com.unlam.verabackend.analysis.domain.model.Message;
import com.unlam.verabackend.analysis.domain.model.Analysis;

public interface AnalyzeTextUseCase {
    Analysis analyzeMessage(Message message);
}
