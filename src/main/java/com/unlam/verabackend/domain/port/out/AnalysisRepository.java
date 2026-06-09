package com.unlam.verabackend.domain.port.out;

import com.unlam.verabackend.domain.model.Analysis;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.Optional;
import java.util.UUID;

public interface AnalysisRepository {
    Analysis save(Analysis analysis);
    void deleteById(UUID id);
    Optional<Analysis> findById(UUID id);
    Page<Analysis> findByUserEmailOrderByCreatedAtDesc(String email, Pageable pageable);
    Page<Analysis> findByUserEmailAndRiskLevelOrderByCreatedAtDesc(String email, String riskLevel, Pageable pageable);
}