package com.unlam.verabackend.analysis.infrastructure.repository;

import com.unlam.verabackend.analysis.domain.model.Analysis;
import com.unlam.verabackend.analysis.domain.ports.out.AnalysisRepositoryPort;
import com.unlam.verabackend.analysis.infrastructure.entity.AnalysisEntity;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class NeonAnalysisRepository implements AnalysisRepositoryPort {

    private final AnalysisJpaRepository jpaRepository;

    public NeonAnalysisRepository(AnalysisJpaRepository jpaRepository) { this.jpaRepository = jpaRepository; }

    @Override
    @Transactional
    public void save(Analysis analysis) {
        AnalysisEntity entity = toEntity(analysis);
        jpaRepository.save(entity);
    }

    private AnalysisEntity toEntity(Analysis analysis) {
        String riskLevelId = analysis.getRiskLevel() != null ? analysis.getRiskLevel().name() : null;
        return new AnalysisEntity(
                analysis.getId(),
                analysis.getMessageId(),
                analysis.isThreat(),
                riskLevelId,
                analysis.getSuspiciousPatterns(),
                analysis.getRecommendation(),
                analysis.getCreatedAt()
        );
    }
}
