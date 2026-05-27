package com.unlam.verabackend.infrastructure.repository;

import com.unlam.verabackend.domain.model.Message;
import com.unlam.verabackend.domain.model.MessageSource;
import com.unlam.verabackend.domain.ports.out.MessageRepositoryPort;
import com.unlam.verabackend.infrastructure.entity.MessageEntity;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class MessageRepositoryAdapter implements MessageRepositoryPort {

    private final MessageJpaRepository jpaRepository;

    public MessageRepositoryAdapter(MessageJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    @Transactional
    public void save(Message domain) {
        if (domain == null) return;
        MessageEntity entity = toEntity(domain);
        jpaRepository.save(entity);
    }

    private MessageEntity toEntity(Message domain) {
        return new MessageEntity(
                domain.getId(),
                domain.getUserId(),
                domain.getContent(),
                domain.getSource() != null ? domain.getSource().name() : MessageSource.UNKNOWN.name(),
                domain.getReceivedAt()
        );
    }
}
