package com.unlam.verabackend.infrastructure.repository;

import com.unlam.verabackend.infrastructure.entity.RiskAlertEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.UUID;

public interface RiskAlertJpaRepository extends JpaRepository<RiskAlertEntity, UUID> {

    @Query("SELECT r FROM RiskAlertEntity r " +
            "JOIN FETCH r.caregiver c " +
            "JOIN FETCH r.analysis a " +
            "JOIN FETCH a.user u " +
            "WHERE c.id = :caregiverId AND r.solved = false")
    List<RiskAlertEntity> findActiveAlertsWithTree(@Param("caregiverId") Long caregiverId);
}
