package com.unlam.verabackend.domain.ports.in;

import com.unlam.verabackend.domain.model.Message;
import com.unlam.verabackend.domain.model.Analysis;

public interface AnalyzeMessageUseCase {
    Analysis analyzeMessage(Message message);
}
