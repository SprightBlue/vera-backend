package com.unlam.verabackend.infrastructure.repository;

import com.unlam.verabackend.infrastructure.entity.TrainingSessionEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface JpaTrainingSessionRepository extends JpaRepository<TrainingSessionEntity, UUID> {
    Page<TrainingSessionEntity> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    @Query("SELECT s FROM TrainingSessionEntity s WHERE s.user.id = :userId AND s.completedAt IS NOT NULL ORDER BY s.completedAt DESC")
    List<TrainingSessionEntity> findCompletedByUserId(@Param("userId") Long userId);

    long countByUserIdAndCompletedAtIsNotNull(Long userId);

    long countByUserIdAndCorrectTrue(Long userId);
}