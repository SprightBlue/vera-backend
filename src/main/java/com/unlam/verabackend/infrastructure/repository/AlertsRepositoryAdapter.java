package com.unlam.verabackend.infrastructure.repository;

import com.unlam.verabackend.domain.model.Alerts;
import com.unlam.verabackend.domain.model.RiskLevel;
import com.unlam.verabackend.domain.port.out.AlertsRepository;
import com.unlam.verabackend.infrastructure.entity.AlertsEntity;
import com.unlam.verabackend.infrastructure.entity.TrustContact;
import com.unlam.verabackend.infrastructure.mapper.AlertsMapper;
import com.unlam.verabackend.infrastructure.repository.jpa.JpaAlertsRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
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
        if (!jpaRepository.existsById(id))
            throw new IllegalArgumentException("No se puede eliminar. Alerta no encontrada con ID: " + id);
        jpaRepository.deleteById(id);
    }

    @Override
    public Optional<Alerts> findById(UUID id) {
        return jpaRepository.findWithRelationshipsById(id).map(mapper::toDomain);
    }

    @Override
    public Page<Alerts> findByCriteria(List<Long> trustContactIds, Boolean isResolved, RiskLevel riskLevel, String search, int page) {
        Pageable pageable = PageRequest.of(page, 10, Sort.by(Sort.Direction.DESC, "createdAt"));
        String cleanSearch = (search != null && !search.trim().isEmpty()) ? search.trim() : null;
        return jpaRepository.filterAlerts(trustContactIds, isResolved, riskLevel, cleanSearch, pageable)
                .map(mapper::toDomain);
    }

    @Override
    public List<Alerts> findTop3ActiveAlertsByCarerEmail(String email) {
        return jpaRepository.findTop3ByTrustContactCarerEmailAndIsResolvedFalseOrderByCreatedAtDesc(email)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public long countAlertsByCarerEmailInLast24Hours(String email) {
        LocalDateTime twentyFourHoursAgo = LocalDateTime.now().minusHours(24);
        return jpaRepository.countByTrustContactCarerEmailAndCreatedAtAfter(email, twentyFourHoursAgo);
    }

    @Override
    public List<Alerts> findTop3ResolvedAlertsByUserEmail(String email) {
        return jpaRepository.findTop3ResolvedAlertsByUserEmail(email)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public long countResolvedAlertsInLast24Hours(String email) {
        LocalDateTime twentyFourHoursAgo = LocalDateTime.now().minusHours(24);
        return jpaRepository.countResolvedAlertsInLast24Hours(email, twentyFourHoursAgo);
    }
}