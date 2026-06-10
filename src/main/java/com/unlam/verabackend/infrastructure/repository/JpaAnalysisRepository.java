package com.unlam.verabackend.infrastructure.repository;

import com.unlam.verabackend.domain.model.RiskLevel;
import com.unlam.verabackend.infrastructure.entity.AnalysisEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;

@Repository
public interface JpaAnalysisRepository extends JpaRepository<AnalysisEntity, UUID> {
    Page<AnalysisEntity> findByUserEmail(String email, Pageable pageable);
    Page<AnalysisEntity> findByUserEmailAndRiskLevel(String user_email, RiskLevel riskLevel, Pageable pageable);
}