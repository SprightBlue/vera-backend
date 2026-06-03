package com.unlam.verabackend.infrastructure.repository;

import com.unlam.verabackend.domain.model.*;
import com.unlam.verabackend.domain.ports.out.AnalysisRepository;
import com.unlam.verabackend.infrastructure.entity.AnalysisEntity;
import com.unlam.verabackend.infrastructure.entity.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.UUID;

@Repository
public class AnalysisRepositoryImpl implements AnalysisRepository {

    private final AnalysisJpaRepository jpaRepository;

    @PersistenceContext
    private EntityManager entityManager;

    public AnalysisRepositoryImpl(AnalysisJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public void save(Analysis analysis) {
        User userRef = entityManager.getReference(User.class, analysis.getUser().getId());

        AnalysisEntity entity = new AnalysisEntity();

        entity.setId(analysis.getId() != null ? analysis.getId() : UUID.randomUUID());
        entity.setUser(userRef);
        entity.setContent(analysis.getContent());
        entity.setContentSourceId(analysis.getMessageSource() != null ? analysis.getMessageSource().name() : MessageSource.UNKNOWN.name());
        entity.setRiskLevelId(analysis.getRiskLevel() != null ? analysis.getRiskLevel().name() : RiskLevel.UNDEFINED.name());
        entity.setSuspiciousPatterns(analysis.getSuspiciousPatterns());
        entity.setRecommendation(analysis.getRecommendation());
        entity.setCreatedAt(analysis.getCreatedAt() != null ? analysis.getCreatedAt() : java.time.LocalDateTime.now());

        jpaRepository.saveAndFlush(entity);
    }

    @Override
    public Optional<Analysis> findById(String id) {
        return jpaRepository.findById(UUID.fromString(id)).map(this::mapToDomain);
    }

    private Analysis mapToDomain(AnalysisEntity entity) {
        User userEntity = entity.getUser();
        DomainUser domainUser = new DomainUser(
                userEntity.getId(),
                userEntity.getFullName(),
                userEntity.getEmail(),
                userEntity.getRole() != null ? Role.valueOf(userEntity.getRole().name()) : null,
                userEntity.getCreatedAt(),
                userEntity.getUpdatedAt(),
                userEntity.isAccountNonLocked(),
                userEntity.isEnabled()
        );

        return new Analysis(
                entity.getId(),
                domainUser,
                entity.getContent(),
                MessageSource.fromString(entity.getContentSourceId()),
                RiskLevel.fromString(entity.getRiskLevelId()),
                entity.getSuspiciousPatterns(),
                entity.getRecommendation(),
                entity.getCreatedAt()
        );
    }
}
