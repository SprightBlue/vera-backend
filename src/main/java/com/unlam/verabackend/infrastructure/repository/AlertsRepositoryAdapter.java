package com.unlam.verabackend.infrastructure.repository;

import com.unlam.verabackend.domain.model.Alerts;
import com.unlam.verabackend.domain.port.out.AlertsRepository;
import com.unlam.verabackend.infrastructure.entity.AlertsEntity;
import com.unlam.verabackend.infrastructure.entity.TrustContact;
import com.unlam.verabackend.infrastructure.mapper.AlertsMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class AlertsRepositoryAdapter implements AlertsRepository {

    private final JpaAlertsRepository jpaAlertsRepository;
    private final TrustContactRepository trustContactRepository;
    private final AlertsMapper mapper;

    public AlertsRepositoryAdapter(JpaAlertsRepository jpaAlertsRepository,
                                   TrustContactRepository trustContactRepository,
                                   AlertsMapper mapper) {
        this.jpaAlertsRepository = jpaAlertsRepository;
        this.trustContactRepository = trustContactRepository;
        this.mapper = mapper;
    }

    @Override
    public Alerts save(Alerts alert, Long trustContactId) {
        TrustContact realContactEntity = trustContactRepository.findById(trustContactId)
                .orElseThrow(() -> new IllegalArgumentException("Contacto de confianza no encontrado con ID: " + trustContactId));

        AlertsEntity entity = mapper.toEntity(alert, realContactEntity);
        AlertsEntity savedEntity = jpaAlertsRepository.save(entity);

        return mapper.toDomain(savedEntity);
    }

    @Override
    public void deleteById(UUID id) {
        if (!jpaAlertsRepository.existsById(id)) {
            throw new IllegalArgumentException("No se puede eliminar. Alerta no encontrada con ID: " + id);
        }
        jpaAlertsRepository.deleteById(id);
    }

    @Override
    public Optional<Alerts> findById(UUID id) {
        return jpaAlertsRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Page<Alerts> findByTrustContactIds(List<Long> trustContactIds, Pageable pageable) {
        return jpaAlertsRepository.findByTrustContactIdIn(trustContactIds, pageable)
                .map(mapper::toDomain);
    }

    @Override
    public Page<Alerts> findByTrustContactIdsAndIsResolved(List<Long> trustContactIds, boolean isResolved, Pageable pageable) {
        return jpaAlertsRepository.findByTrustContactIdInAndIsResolved(trustContactIds, isResolved, pageable)
                .map(mapper::toDomain);
    }
}