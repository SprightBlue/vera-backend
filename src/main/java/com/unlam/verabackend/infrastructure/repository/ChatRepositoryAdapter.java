package com.unlam.verabackend.infrastructure.repository;

import com.unlam.verabackend.domain.model.Chats;
import com.unlam.verabackend.domain.port.out.ChatsRepository;
import com.unlam.verabackend.infrastructure.entity.ChatsEntity;
import com.unlam.verabackend.infrastructure.entity.AnalysisEntity;
import com.unlam.verabackend.infrastructure.entity.User;
import com.unlam.verabackend.infrastructure.mapper.ChatsMapper;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ChatRepositoryAdapter implements ChatsRepository {

    private final JpaChatsRepository jpaChatsRepository;
    private final ChatsMapper chatsMapper;
    private final EntityManager entityManager;

    @Override
    public Chats save(Chats chat) {
        User userProxy = entityManager.getReference(User.class, chat.getUser().getId());

        AnalysisEntity analysisProxy = chat.getAnalysis() != null
                ? entityManager.getReference(AnalysisEntity.class, chat.getAnalysis().getId())
                : null;

        ChatsEntity entity = chatsMapper.toEntity(chat, userProxy, analysisProxy);
        ChatsEntity savedEntity = jpaChatsRepository.save(entity);
        entityManager.flush();
        return chatsMapper.toDomain(savedEntity);
    }

    @Override
    public Optional<Chats> findById(UUID id) {
        return jpaChatsRepository.findByIdWithAnalysis(id).map(chatsMapper::toDomain);
    }

    @Override
    public List<Chats> findByUserEmail(String email) {
        return jpaChatsRepository.findByUserEmailOrderByUpdatedAtDesc(email)
                .stream()
                .map(chatsMapper::toDomain)
                .toList();
    }

    @Override
    public void deleteById(UUID id) {
        jpaChatsRepository.deleteById(id);
    }
}