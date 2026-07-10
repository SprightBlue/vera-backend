package com.unlam.verabackend.infrastructure.repository.jpa;

import com.unlam.verabackend.domain.model.RiskLevel;
import com.unlam.verabackend.infrastructure.entity.AlertsEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
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
    @Query("""
       SELECT a FROM AlertsEntity a
       WHERE a.trustContact.id IN :trustContactIds
         AND (:isResolved IS NULL OR a.isResolved = :isResolved)
         AND (:riskLevel IS NULL OR a.riskLevel = :riskLevel)
         AND (:search IS NULL OR :search = ''
              OR LOWER(a.title) LIKE LOWER(CONCAT('%', :search, '%'))
              OR LOWER(a.contentSummary) LIKE LOWER(CONCAT('%', :search, '%')))
    """)
    Page<AlertsEntity> filterAlerts(
            @Param("trustContactIds") List<Long> trustContactIds,
            @Param("isResolved") Boolean isResolved,
            @Param("riskLevel") RiskLevel riskLevel,
            @Param("search") String search,
            Pageable pageable
    );
    List<AlertsEntity> findTop3ByTrustContactCarerEmailAndIsResolvedFalseOrderByCreatedAtDesc(String email);
    @Query("""
       SELECT COUNT(a) FROM AlertsEntity a
       WHERE (a.trustContact.carer.email = :email OR a.trustContact.protectedUser.email = :email)
         AND a.createdAt >= :date
    """)
    long countAlertsByEmailSince(@Param("email") String email, @Param("date") LocalDateTime date);
    @Query("""
       SELECT COUNT(a) FROM AlertsEntity a
       WHERE (a.trustContact.carer.email = :email OR a.trustContact.protectedUser.email = :email)
         AND a.isResolved = true
         AND a.resolvedAt >= :date
    """)
    long countResolvedAlertsByEmailSince(@Param("email") String email, @Param("date") LocalDateTime date);

    long countByTrustContactIdAndRiskLevelAndCreatedAtAfter(Long trustContactId, String riskLevel, LocalDateTime date);
    List<AlertsEntity> findTop3ByTrustContactIdAndCreatedAtAfterOrderByCreatedAtDesc(Long trustContactId, LocalDateTime date);
}