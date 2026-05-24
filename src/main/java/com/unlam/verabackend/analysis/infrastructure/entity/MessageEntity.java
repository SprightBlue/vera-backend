package com.unlam.verabackend.analysis.infrastructure.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Setter
@Getter
@Entity
@Table(name = "messages")
public class MessageEntity {

    @Id
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(columnDefinition = "text", nullable = false)
    private String content;

    @Column(name = "source_id", length = 50, nullable = false)
    private String sourceId;

    @Column(name = "received_at", nullable = false)
    private LocalDateTime receivedAt;

    public MessageEntity() {
    }

    public MessageEntity(UUID id, Long userId, String content, String sourceId, LocalDateTime receivedAt) {
        this.id = id;
        this.userId = userId;
        this.content = content;
        this.sourceId = sourceId;
        this.receivedAt = receivedAt;
    }

}
