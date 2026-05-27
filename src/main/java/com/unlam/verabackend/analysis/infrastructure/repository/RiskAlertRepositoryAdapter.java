package com.unlam.verabackend.analysis.infrastructure.repository;

import com.unlam.verabackend.analysis.domain.model.RiskAlert;
import com.unlam.verabackend.analysis.domain.ports.out.RiskAlertRepositoryPort;
import com.unlam.verabackend.analysis.infrastructure.entity.AnalysisEntity;
import com.unlam.verabackend.analysis.infrastructure.entity.RiskAlertEntity;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Repository
public class RiskAlertRepositoryAdapter implements RiskAlertRepositoryPort {

    private final RiskAlertJpaRepository jpaRepository;
    private final AnalysisJpaRepository analysisJpaRepository;

    public RiskAlertRepositoryAdapter(RiskAlertJpaRepository jpaRepository, AnalysisJpaRepository analysisJpaRepository) {
        this.jpaRepository = jpaRepository;
        this.analysisJpaRepository = analysisJpaRepository;
    }

    @Override
    @Transactional
    public RiskAlert save(RiskAlert domain) {
        if (domain == null) return null;

        RiskAlertEntity entity = toEntity(domain);
        jpaRepository.save(entity);

        domain.setId(entity.getId());
        return domain;
    }

    @Override
    @Transactional(readOnly = true)
    public RiskAlert findById(UUID id) {
        return jpaRepository.findById(id).map(this::toDomain).orElse(null);
    }

    private RiskAlert toDomain(RiskAlertEntity entity) {
        return new RiskAlert(
                entity.getId(),
                entity.getAnalysis().getId(),
                entity.getCaregiverId(),
                entity.isReceived(),
                entity.getCreatedAt()
        );
    }

    private RiskAlertEntity toEntity(RiskAlert domain) {
        AnalysisEntity analysisRef = analysisJpaRepository.getReferenceById(domain.getAnalysisId());

        return new RiskAlertEntity(
                domain.getId(),
                analysisRef,
                domain.getCaregiverId(),
                domain.isReceived(),
                domain.getCreatedAt()
        );
    }
}
