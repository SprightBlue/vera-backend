package com.unlam.verabackend.infrastructure.repository;

import com.unlam.verabackend.domain.model.*;
import com.unlam.verabackend.domain.ports.out.RiskAlertRepository;
import com.unlam.verabackend.infrastructure.entity.AnalysisEntity;
import com.unlam.verabackend.infrastructure.entity.RiskAlertEntity;
import com.unlam.verabackend.infrastructure.entity.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
public class RiskAlertRepositoryImpl implements RiskAlertRepository {

    private final RiskAlertJpaRepository jpaRepository;

    @PersistenceContext
    private final EntityManager entityManager;

    public RiskAlertRepositoryImpl(RiskAlertJpaRepository jpaRepository, EntityManager entityManager) {
        this.jpaRepository = jpaRepository;
        this.entityManager = entityManager;
    }

    @Override
    public List<RiskAlert> findActiveByCarer(Long carerId) { // 👈 Renombrado de Caregiver a Carer
        return jpaRepository.findActiveAlertsWithTree(carerId).stream()
                .map(this::mapToDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<RiskAlert> findActiveByCarerEmail(String email) { // 👈 Renombrado de CaregiverEmail a CarerEmail
        return jpaRepository.findActiveAlertsWithTreeByEmail(email).stream()
                .map(this::mapToDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<RiskAlert> findById(String id) {
        return jpaRepository.findById(UUID.fromString(id)).map(this::mapToDomain);
    }

    @Override
    public RiskAlert save(RiskAlert riskAlert) {
        AnalysisEntity analysisRef = entityManager.getReference(AnalysisEntity.class, riskAlert.getAnalysis().getId());
        // Se asume que en el modelo de dominio 'riskAlert.getCaregiver()' ahora representa textualmente al Carer asignado
        User carerRef = entityManager.getReference(User.class, riskAlert.getCaregiver().getId());

        RiskAlertEntity entity = new RiskAlertEntity();

        entity.setId(riskAlert.getId() != null ? riskAlert.getId() : UUID.randomUUID());
        entity.setAnalysis(analysisRef);
        entity.setCaregiver(carerRef); // 👈 Si cambiaste la propiedad en RiskAlertEntity, actualizala acá a setCarer(carerRef)
        entity.setSolved(riskAlert.isSolved());
        entity.setCreatedAt(riskAlert.getCreatedAt() != null ? riskAlert.getCreatedAt() : java.time.LocalDateTime.now());

        // 👈 CAMBIO CRÍTICO: Asegura la persistencia inmediata en la base de datos
        jpaRepository.saveAndFlush(entity);

        return mapToDomain(entity);
    }

    private RiskAlert mapToDomain(RiskAlertEntity entity) {
        var analysisEntity = entity.getAnalysis();

        DomainUser domainUser = mapUserToDomain(analysisEntity.getUser());
        DomainUser domainCarer = mapUserToDomain(entity.getCaregiver()); // 👈 Renombrado conceptualmente a domainCarer

        Analysis analysis = new Analysis(
                analysisEntity.getId(),
                domainUser,
                analysisEntity.getContent(),
                MessageSource.fromString(analysisEntity.getContentSourceId()),
                RiskLevel.fromString(analysisEntity.getRiskLevelId()),
                analysisEntity.getSuspiciousPatterns(),
                analysisEntity.getRecommendation(),
                analysisEntity.getCreatedAt()
        );

        return new RiskAlert(entity.getId(), analysis, domainCarer, entity.isSolved(), entity.getCreatedAt());
    }

    private DomainUser mapUserToDomain(User entity) {
        return new DomainUser(
                entity.getId(),
                entity.getFullName(),
                entity.getEmail(),
                entity.getRole() != null ? Role.valueOf(entity.getRole().name()) : null,
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.isAccountNonLocked(),
                entity.isEnabled()
        );
    }
}