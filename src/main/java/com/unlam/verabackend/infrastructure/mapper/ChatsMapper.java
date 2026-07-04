package com.unlam.verabackend.infrastructure.mapper;

import com.unlam.verabackend.domain.model.Chats;
import com.unlam.verabackend.infrastructure.entity.AnalysisEntity;
import com.unlam.verabackend.infrastructure.entity.ChatsEntity;
import com.unlam.verabackend.infrastructure.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ChatsMapper {

    private final AnalysisMapper analysisMapper;

    public ChatsEntity toEntity(Chats domain, User userEntity, AnalysisEntity analysisEntity) {
        if (domain == null) return null;

        return ChatsEntity.builder()
                .id(domain.getId())
                .user(userEntity)
                .analysis(analysisEntity)
                .title(domain.getTitle())
                .build();
    }

    public Chats toDomain(ChatsEntity entity) {
        if (entity == null) return null;

        return Chats.builder()
                .id(entity.getId())
                .user(entity.getUser())
                .analysis(analysisMapper.toDomain(entity.getAnalysis()))
                .title(entity.getTitle())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}