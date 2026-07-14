package com.unlam.verabackend.domain.port.in;

import com.unlam.verabackend.domain.model.Analysis;
import com.unlam.verabackend.domain.model.RiskLevel;
import org.springframework.data.domain.Page;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

public interface ManageAnalysisUseCase {
    @Transactional
    Page<Analysis> getAnalysisHistory(String email, RiskLevel riskLevel, String search, int page);

    @Transactional
    Analysis getAnalysisDetail(UUID id, String userEmail);

    @Transactional
    void deleteAnalysis(UUID id, String userEmail);
}