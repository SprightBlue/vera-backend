package com.unlam.verabackend.infrastructure.repository;

import com.unlam.verabackend.infrastructure.entity.ChatMessagesEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface JpaChatMessagesRepository extends JpaRepository<ChatMessagesEntity, UUID> {
    List<ChatMessagesEntity> findByChatIdOrderByCreatedAtAsc(UUID chatId);
}