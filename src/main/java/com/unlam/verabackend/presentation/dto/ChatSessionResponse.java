package com.unlam.verabackend.presentation.dto;

import com.unlam.verabackend.domain.model.Chats;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class ChatSessionResponse {

    private UUID id;
    private String title;

    public static ChatSessionResponse fromDomain(Chats chat) {
        if (chat == null) {
            return null;
        }
        return ChatSessionResponse.builder()
                .id(chat.getId())
                .title(chat.getTitle() != null ? chat.getTitle() : null)
                .build();
    }
}