package com.unlam.verabackend.domain.port.out;

import com.unlam.verabackend.domain.model.Analysis;
import com.unlam.verabackend.domain.model.RiskLevel;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AnalysisRepository {
    Analysis save(Analysis analysis);
    void deleteById(UUID id);
    Optional<Analysis> findById(UUID id);
    Page<Analysis> findByCriteria(String email, RiskLevel riskLevel, String search, int page);
    List<Analysis> findTop3ByUserEmail(String email);
    long countByUserEmailInLast24Hours(String email);
}