package com.unlam.verabackend.domain.port.in;

import com.unlam.verabackend.domain.model.Analysis;
import com.unlam.verabackend.domain.model.RiskLevel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.UUID;

public interface ManageAnalysisUseCase {
    Page<Analysis> getHistoryByUserEmail(String email, Pageable pageable);
    Page<Analysis> getHistoryByUserEmailAndRiskLevel(String email, RiskLevel riskLevel, Pageable pageable);
    Analysis getAnalysisDetail(UUID id, String userEmail);
    void deleteAnalysis(UUID id, String userEmail);
}