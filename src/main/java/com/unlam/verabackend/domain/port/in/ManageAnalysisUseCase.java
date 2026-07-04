package com.unlam.verabackend.domain.port.in;

import com.unlam.verabackend.domain.model.Analysis;
import com.unlam.verabackend.domain.model.RiskLevel;
import org.springframework.data.domain.Page;

import java.util.UUID;

public interface ManageAnalysisUseCase {
    Page<Analysis> getAnalysisHistory(String email, RiskLevel riskLevel, String search, int page);
    Analysis getAnalysisDetail(UUID id, String userEmail);
    void deleteAnalysis(UUID id, String userEmail);
}