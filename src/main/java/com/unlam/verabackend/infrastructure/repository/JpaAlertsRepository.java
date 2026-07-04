package com.unlam.verabackend.infrastructure.repository;

import com.unlam.verabackend.infrastructure.entity.AlertsEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface JpaAlertsRepository extends JpaRepository<AlertsEntity, UUID>, JpaSpecificationExecutor<AlertsEntity> {

    @EntityGraph(attributePaths = {"trustContact", "trustContact.carer", "trustContact.protectedUser"})
    Optional<AlertsEntity> findWithRelationshipsById(UUID id);

    long countByTrustContactIdAndRiskLevelAndCreatedAtAfter(Long trustContactId, String riskLevel, LocalDateTime date);
    List<AlertsEntity> findTop3ByTrustContactIdAndCreatedAtAfterOrderByCreatedAtDesc(Long trustContactId, LocalDateTime date);
}