package com.unlam.verabackend.analysis.infrastructure.repository;

import com.unlam.verabackend.analysis.domain.model.Message;
import com.unlam.verabackend.analysis.domain.ports.out.MessageRepositoryPort;
import com.unlam.verabackend.analysis.infrastructure.entity.MessageEntity;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class NeonMessageRepository implements MessageRepositoryPort {

    private final MessageJpaRepository jpaRepository;

    public NeonMessageRepository(MessageJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    @Transactional
    public void save(Message message) {
        MessageEntity entity = toEntity(message);
        jpaRepository.save(entity);
    }

    private MessageEntity toEntity(Message message) {
        String sourceId = message.getSource() != null ? message.getSource().name() : "UNKNOWN";
        return new MessageEntity(
                message.getId(),
                message.getUserId(),
                message.getContent(),
                sourceId,
                message.getReceivedAt()
        );
    }
}
