package com.unlam.verabackend.infrastructure.repository;

import com.unlam.verabackend.domain.model.ChatMessages;
import com.unlam.verabackend.domain.port.out.ChatMessagesRepository;
import com.unlam.verabackend.infrastructure.entity.ChatMessagesEntity;
import com.unlam.verabackend.infrastructure.entity.ChatsEntity;
import com.unlam.verabackend.infrastructure.mapper.ChatMessagesMapper;
import com.unlam.verabackend.infrastructure.repository.JpaChatMessagesRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

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

        return chatMessagesMapper.toDomain(savedEntity);
    }

    @Override
    public List<ChatMessages> findByChatId(UUID chatId) {
        return jpaChatMessageRepository.findByChatIdOrderByCreatedAtAsc(chatId)
                .stream()
                .map(chatMessagesMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<ChatMessages> findLastMessages(UUID chatId, int limit) {
        List<ChatMessagesEntity> entities = jpaChatMessageRepository.findTop10ByChatIdOrderByCreatedAtDesc(chatId);

        return entities.stream()
                .map(chatMessagesMapper::toDomain)
                .collect(Collectors.collectingAndThen(
                        Collectors.toList(),
                        list -> { Collections.reverse(list); return list; }
                ));
    }
}