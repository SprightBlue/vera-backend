package com.unlam.verabackend.infrastructure.repository;

import com.unlam.verabackend.domain.model.Analysis;
import com.unlam.verabackend.domain.model.RiskLevel;
import com.unlam.verabackend.domain.ports.out.AnalysisRepositoryPort;
import com.unlam.verabackend.infrastructure.entity.AnalysisEntity;
import com.unlam.verabackend.infrastructure.entity.MessageEntity;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class AnalysisRepositoryAdapter implements AnalysisRepositoryPort {

    private final AnalysisJpaRepository jpaRepository;
    private final MessageJpaRepository messageJpaRepository;

    public AnalysisRepositoryAdapter(AnalysisJpaRepository jpaRepository, MessageJpaRepository messageJpaRepository) {
        this.jpaRepository = jpaRepository;
        this.messageJpaRepository = messageJpaRepository;
    }

    @Override
    @Transactional
    public void save(Analysis domain) {
        if (domain == null) return;
        AnalysisEntity entity = toEntity(domain);
        jpaRepository.save(entity);
    }

    private AnalysisEntity toEntity(Analysis domain) {
        MessageEntity messageRef = messageJpaRepository.getReferenceById(domain.getMessageId());

        return new AnalysisEntity(
                domain.getId(),
                messageRef,
                domain.getRiskLevel() != null ? domain.getRiskLevel().name() : RiskLevel.UNDEFINED.name(),
                domain.getSuspiciousPatterns(),
                domain.getRecommendation(),
                domain.getCreatedAt()
        );
    }
}
