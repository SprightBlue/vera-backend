package com.unlam.verabackend.infrastructure.repository.jpa;

import com.unlam.verabackend.infrastructure.entity.IncidentStepEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.UUID;

@Repository
public interface JpaIncidentStepRepository extends JpaRepository<IncidentStepEntity, UUID> {
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE IncidentStepEntity s SET s.completed = true, s.completedAt = :completedAt WHERE s.id = :id")
    void markCompleted(@Param("id") UUID id, @Param("completedAt") LocalDateTime completedAt);
}