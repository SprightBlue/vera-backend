package com.unlam.verabackend.infrastructure.repository.jpa;

import com.unlam.verabackend.domain.model.RiskLevel;
import com.unlam.verabackend.infrastructure.entity.AnalysisEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface JpaAnalysisRepository extends JpaRepository<AnalysisEntity, UUID> {
    @Query("""
       SELECT a FROM AnalysisEntity a
       WHERE a.user.email = :email
         AND (:riskLevel IS NULL OR a.riskLevel = :riskLevel)
         AND (:search IS NULL OR :search = ''
              OR LOWER(a.title) LIKE LOWER(CONCAT('%', :search, '%'))
              OR LOWER(a.contentSummary) LIKE LOWER(CONCAT('%', :search, '%')))
    """)
    Page<AnalysisEntity> filterAnalysis(
            @Param("email") String email,
            @Param("riskLevel") RiskLevel riskLevel,
            @Param("search") String search,
            Pageable pageable
    );
    List<AnalysisEntity> findTop3ByUserEmailOrderByCreatedAtDesc(String email);
    long countByUserEmailAndCreatedAtAfter(String email, LocalDateTime dateTime);
}