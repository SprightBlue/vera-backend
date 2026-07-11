package com.unlam.verabackend.infrastructure.repository.jpa;

import com.unlam.verabackend.infrastructure.entity.TrainingScenarioEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface JpaTrainingScenarioRepository extends JpaRepository<TrainingScenarioEntity, UUID> {
    List<TrainingScenarioEntity> findByActiveTrue();
}