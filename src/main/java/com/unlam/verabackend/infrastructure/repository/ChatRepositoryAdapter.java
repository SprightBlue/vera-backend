package com.unlam.verabackend.infrastructure.repository;

import com.unlam.verabackend.domain.model.Chats;
import com.unlam.verabackend.domain.port.out.ChatsRepository;
import com.unlam.verabackend.infrastructure.entity.ChatsEntity;
import com.unlam.verabackend.infrastructure.entity.User;
import com.unlam.verabackend.infrastructure.mapper.ChatsMapper;
import com.unlam.verabackend.infrastructure.repository.JpaChatsRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ChatRepositoryAdapter implements ChatsRepository {

    private final JpaChatsRepository jpaChatsRepository;
    private final ChatsMapper chatsMapper;
    private final EntityManager entityManager;

    @Override
    public Chats save(Chats chat) {
        User userEntity = entityManager.getReference(User.class, chat.getUser().getId());

        var analysisEntity = chat.getAnalysis() != null
                ? entityManager.getReference(com.unlam.verabackend.infrastructure.entity.AnalysisEntity.class, chat.getAnalysis().getId())
                : null;

        var alertsEntity = chat.getAlert() != null
                ? entityManager.getReference(com.unlam.verabackend.infrastructure.entity.AlertsEntity.class, chat.getAlert().getId())
                : null;

        ChatsEntity entity = chatsMapper.toEntity(chat, userEntity, analysisEntity, alertsEntity);
        ChatsEntity savedEntity = jpaChatsRepository.save(entity);

        return chatsMapper.toDomain(savedEntity);
    }

    @Override
    public Optional<Chats> findById(UUID id) {
        return jpaChatsRepository.findById(id).map(chatsMapper::toDomain);
    }

    @Override
    public List<Chats> findByUserEmail(String email) {
        return jpaChatsRepository.findByUserEmailOrderByUpdatedAtDesc(email)
                .stream()
                .map(chatsMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Chats> findByAnalysisId(UUID analysisId) {
        return jpaChatsRepository.findByAnalysisId(analysisId).map(chatsMapper::toDomain);
    }

    @Override
    public Optional<Chats> findByAlertId(UUID alertId) {
        return jpaChatsRepository.findByAlertId(alertId).map(chatsMapper::toDomain);
    }
}