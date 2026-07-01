package com.unlam.verabackend.presentation.dto;

import com.unlam.verabackend.domain.model.ChatMessages;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ChatMessagesResponse {

    private String role;
    private String content;

    public static ChatMessagesResponse fromDomain(ChatMessages message) {
        if (message == null) {
            return null;
        }
        return ChatMessagesResponse.builder()
                .role(message.getRole() != null ? message.getRole().name() : null)
                .content(message.getContent())
                .build();
    }
}