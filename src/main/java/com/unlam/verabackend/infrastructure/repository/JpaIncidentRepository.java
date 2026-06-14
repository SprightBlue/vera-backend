package com.unlam.verabackend.infrastructure.repository;

import com.unlam.verabackend.infrastructure.entity.IncidentEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface JpaIncidentRepository extends JpaRepository<IncidentEntity, UUID> {

    @EntityGraph(attributePaths = {"steps", "user"})
    Optional<IncidentEntity> findWithStepsById(UUID id);

    Page<IncidentEntity> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE IncidentEntity i SET i.status = 'COMPLETED', i.completedAt = :completedAt WHERE i.id = :id")
    void markCompleted(@Param("id") UUID id, @Param("completedAt") LocalDateTime completedAt);
}