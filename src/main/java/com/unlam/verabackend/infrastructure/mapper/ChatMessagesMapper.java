package com.unlam.verabackend.infrastructure.mapper;

import com.unlam.verabackend.domain.model.ChatMessages;
import com.unlam.verabackend.domain.model.ChatsRole;
import com.unlam.verabackend.infrastructure.entity.ChatsEntity;
import com.unlam.verabackend.infrastructure.entity.ChatMessagesEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ChatMessagesMapper {

    private final ChatsMapper chatMapper;

    public ChatMessagesEntity toEntity(ChatMessages domain, ChatsEntity chatEntity) {
        if (domain == null) return null;

        return ChatMessagesEntity.builder()
                .id(domain.getId())
                .chat(chatEntity)
                .role(domain.getRole() != null ? domain.getRole().name() : null)
                .content(domain.getContent())
                .createdAt(domain.getCreatedAt())
                .build();
    }

    public ChatMessages toDomain(ChatMessagesEntity entity) {
        if (entity == null) return null;

        return ChatMessages.builder()
                .id(entity.getId())
                .chat(chatMapper.toDomain(entity.getChat()))
                .role(entity.getRole() != null ? ChatsRole.valueOf(entity.getRole()) : null)
                .content(entity.getContent())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}