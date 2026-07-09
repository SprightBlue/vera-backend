package com.unlam.verabackend.infrastructure.repository.jpa;

import com.unlam.verabackend.infrastructure.entity.ChatsEntity;
import org.jspecify.annotations.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface JpaChatsRepository extends JpaRepository<ChatsEntity, UUID> {
    List<ChatsEntity> findByUserEmailOrderByUpdatedAtDesc(String email);
    boolean existsById(@NonNull UUID id);

    @Query("SELECT c FROM ChatsEntity c LEFT JOIN FETCH c.analysis WHERE c.id = :id")
    Optional<ChatsEntity> findByIdWithAnalysis(@Param("id") UUID id);
}