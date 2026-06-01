package com.unlam.verabackend.infrastructure.repository;

import com.unlam.verabackend.infrastructure.entity.AnalysisEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface AnalysisJpaRepository extends JpaRepository<AnalysisEntity, UUID> {
}
