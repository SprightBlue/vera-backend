package com.unlam.verabackend.presentation.dto;

import com.unlam.verabackend.domain.model.ChatMessages;
import com.unlam.verabackend.domain.model.ChatsRole;
import java.time.LocalDateTime;

public record ChatMessagesResponse(
        ChatsRole role,
        String content,
        LocalDateTime createdAt
) {
    public static ChatMessagesResponse fromDomain(ChatMessages message) {
        if (message == null) return null;
        return new ChatMessagesResponse(
                message.getRole(),
                message.getContent(),
                message.getCreatedAt()
        );
    }
}