package com.unlam.verabackend.infrastructure.repository;

import com.unlam.verabackend.domain.model.ChatMessages;
import com.unlam.verabackend.domain.port.out.ChatMessagesRepository;
import com.unlam.verabackend.infrastructure.entity.ChatMessagesEntity;
import com.unlam.verabackend.infrastructure.entity.ChatsEntity;
import com.unlam.verabackend.infrastructure.mapper.ChatMessagesMapper;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ChatMessageRepositoryAdapter implements ChatMessagesRepository {

    private final JpaChatMessagesRepository jpaChatMessageRepository;
    private final ChatMessagesMapper chatMessagesMapper;
    private final EntityManager entityManager;

    @Override
    public ChatMessages save(ChatMessages message) {
        ChatsEntity chatEntity = entityManager.getReference(ChatsEntity.class, message.getChat().getId());

        ChatMessagesEntity entity = chatMessagesMapper.toEntity(message, chatEntity);
        ChatMessagesEntity savedEntity = jpaChatMessageRepository.save(entity);

        entityManager.flush();

        return chatMessagesMapper.toDomain(savedEntity);
    }

    @Override
    public List<ChatMessages> findByChatId(UUID chatId) {
        return jpaChatMessageRepository.findByChatIdOrderByCreatedAtAsc(chatId)
                .stream()
                .map(chatMessagesMapper::toDomain)
                .toList();
    }

    @Override
    public List<ChatMessages> findLastMessages(UUID chatId) {
        List<ChatMessagesEntity> entities = jpaChatMessageRepository.findLastMessages(
                chatId,
                PageRequest.of(0, 10)
        );

        ArrayList<ChatMessagesEntity> mutableEntities = new ArrayList<>(entities);
        Collections.reverse(mutableEntities);

        return mutableEntities.stream()
                .map(chatMessagesMapper::toDomain)
                .toList();
    }
}