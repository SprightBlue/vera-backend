package com.unlam.verabackend.infrastructure.repository;

import com.unlam.verabackend.infrastructure.entity.RiskAlertEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface RiskAlertJpaRepository extends JpaRepository<RiskAlertEntity, UUID> {
}
