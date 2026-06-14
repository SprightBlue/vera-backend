package com.unlam.verabackend.infrastructure.repository;

import com.unlam.verabackend.domain.model.Alerts;
import com.unlam.verabackend.domain.port.out.AlertsRepository;
import com.unlam.verabackend.infrastructure.entity.AlertsEntity;
import com.unlam.verabackend.infrastructure.entity.TrustContact;
import com.unlam.verabackend.infrastructure.mapper.AlertsMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class AlertsRepositoryAdapter implements AlertsRepository {

    private final JpaAlertsRepository jpaRepository;
    private final TrustContactRepository trustContactRepository;
    private final AlertsMapper mapper;

    @Override
    public Alerts save(Alerts alert, Long trustContactId) {
        TrustContact realContactEntity = trustContactRepository.findById(trustContactId)
                .orElseThrow(() -> new IllegalArgumentException("Contacto de confianza no encontrado con ID: " + trustContactId));

        AlertsEntity entity = mapper.toEntity(alert, realContactEntity);
        AlertsEntity savedEntity = jpaRepository.save(entity);

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
    public Page<Alerts> findByTrustContactIdsCreatedAtDesc(List<Long> trustContactIds, Pageable pageable) {
        return jpaRepository.findByTrustContactIdIn(trustContactIds, pageable)
                .map(mapper::toDomain);
    }

    @Override
    public Page<Alerts> findByTrustContactIdsAndIsResolvedCreatedAtDesc(List<Long> trustContactIds, boolean isResolved, Pageable pageable) {
        return jpaRepository.findByTrustContactIdInAndIsResolved(trustContactIds, isResolved, pageable)
                .map(mapper::toDomain);
    }

    @Override
    public void resolveAlertDirectly(UUID id, LocalDateTime resolvedAt) {
        jpaRepository.resolveAlertById(id, resolvedAt);
    }
}