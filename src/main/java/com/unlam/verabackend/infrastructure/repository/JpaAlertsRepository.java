package com.unlam.verabackend.infrastructure.repository;

import com.unlam.verabackend.infrastructure.entity.AlertsEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface JpaAlertsRepository extends JpaRepository<AlertsEntity, UUID> {
    @EntityGraph(attributePaths = {"trustContact", "trustContact.carer", "trustContact.protectedUser"})
    Optional<AlertsEntity> findWithRelationshipsById(UUID id);
    Page<AlertsEntity> findByTrustContactIdIn(List<Long> trustContactIds, Pageable pageable);
    Page<AlertsEntity> findByTrustContactIdInAndIsResolved(List<Long> trustContactIds, boolean isResolved, Pageable pageable);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE AlertsEntity a SET a.isResolved = true, a.resolvedAt = :resolvedAt WHERE a.id = :id")
    void resolveAlertById(@Param("id") UUID id, @Param("resolvedAt") LocalDateTime resolvedAt);
}