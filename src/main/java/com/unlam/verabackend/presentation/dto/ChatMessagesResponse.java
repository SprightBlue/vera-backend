package com.unlam.verabackend.presentation.dto;

import com.unlam.verabackend.domain.model.ChatMessages;
import com.unlam.verabackend.domain.model.ChatsRole;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ChatMessagesResponse {

    private ChatsRole role;
    private String content;
    private LocalDateTime createdAt;

    public static ChatMessagesResponse fromDomain(ChatMessages message) {
        if (message == null) {
            return null;
        }
        return ChatMessagesResponse.builder()
                .role(message.getRole())
                .content(message.getContent())
                .createdAt(message.getCreatedAt())
                .build();
    }
}