package com.unlam.verabackend.domain.ports.in;

import com.unlam.verabackend.domain.model.Analysis;
import com.unlam.verabackend.domain.model.MessageSource;
import com.unlam.verabackend.domain.model.DomainUser;

public interface AnalyzeMessageUseCase {
    Analysis analyzeMessage(DomainUser domainUser, String content, MessageSource source);
}
