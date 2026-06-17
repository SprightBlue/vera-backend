package com.unlam.verabackend.infrastructure.repository;

import com.unlam.verabackend.infrastructure.entity.ChatsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface JpaChatsRepository extends JpaRepository<ChatsEntity, UUID> {
    List<ChatsEntity> findByUserEmailOrderByUpdatedAtDesc(String email);
    Optional<ChatsEntity> findByAnalysisId(UUID analysisId);
    Optional<ChatsEntity> findByAlertId(UUID alertId);
}