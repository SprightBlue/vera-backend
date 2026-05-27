package com.unlam.verabackend.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Message {
    private UUID id;
    private Long userId;
    private String content;
    private MessageSource source;
    private LocalDateTime receivedAt;

    public MessageSource getSource() {
        return this.source != null ? this.source : MessageSource.UNKNOWN;
    }
}
