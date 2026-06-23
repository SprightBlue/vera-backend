package com.unlam.verabackend.domain.model;

import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessages {
    private UUID id;
    private Chats chat;
    private ChatsRole role;
    private String content;
    private LocalDateTime createdAt;
}