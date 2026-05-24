package com.unlam.verabackend.analysis.domain.model;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Setter
@Getter
public class Message {
    private UUID id;
    private Long userId;
    private String content;
    private MessageSource source;
    private LocalDateTime receivedAt;

    public Message(UUID id, Long userId, String content, MessageSource source, LocalDateTime receivedAt) {
        this.id = id;
        this.userId = userId;
        this.content = content;
        this.source = source;
        this.receivedAt = receivedAt;
    }

}
