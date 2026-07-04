package com.unlam.verabackend.infrastructure.repository;

import com.unlam.verabackend.domain.model.Alerts;
import com.unlam.verabackend.domain.model.RiskLevel;
import com.unlam.verabackend.domain.port.out.AlertsRepository;
import com.unlam.verabackend.infrastructure.entity.AlertsEntity;
import com.unlam.verabackend.infrastructure.entity.TrustContact;
import com.unlam.verabackend.infrastructure.mapper.AlertsMapper;
import com.unlam.verabackend.infrastructure.repository.specification.AlertsSpecification;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class AlertsRepositoryAdapter implements AlertsRepository {

    private final JpaAlertsRepository jpaRepository;
    private final AlertsMapper mapper;
    private final EntityManager entityManager;

    @Override
    public Alerts save(Alerts alert, Long trustContactId) {
        TrustContact trustContactProxy = entityManager.getReference(TrustContact.class, trustContactId);
        AlertsEntity entity = mapper.toEntity(alert, trustContactProxy);
        AlertsEntity savedEntity = jpaRepository.save(entity);
        entityManager.flush();
        return mapper.toDomain(savedEntity);
    }

    @Override
    public void deleteById(UUID id) {
        if (!jpaRepository.existsById(id)) {
            throw new IllegalArgumentException("No se puede eliminar. Alerta no encontrada con ID: " + id);
        }
        jpaRepository.deleteById(id);
    }

    @Override
    public Optional<Alerts> findById(UUID id) {
        return jpaRepository.findWithRelationshipsById(id).map(mapper::toDomain);
    }

    @Override
    public void resolveAlert(UUID id, LocalDateTime resolvedAt) {
        AlertsEntity entity = jpaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Alerta no encontrada con ID: " + id));
        entity.setResolved(true);
        if (resolvedAt != null) {
            entity.setResolvedAt(resolvedAt);
        }
        jpaRepository.save(entity);
    }

    @Override
    public Page<Alerts> findByCriteria(List<Long> trustContactIds, Boolean isResolved, RiskLevel riskLevel, String search, int page) {
        Pageable pageable = PageRequest.of(page, 10);
        Specification<AlertsEntity> spec = AlertsSpecification.filterAlerts(trustContactIds, isResolved, riskLevel, search);
        return jpaRepository.findAll(spec, pageable).map(mapper::toDomain);
    }
}