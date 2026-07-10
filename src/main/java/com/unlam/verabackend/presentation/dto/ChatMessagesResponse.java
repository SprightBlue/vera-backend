package com.unlam.verabackend.presentation.dto;

import com.unlam.verabackend.domain.model.ChatMessages;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "DTO que representa un mensaje individual dentro de la conversación con la IA")
public class ChatMessagesResponse {

    @Schema(description = "Rol del emisor del mensaje en el contexto de la IA", example = "USER", allowableValues = {"USER", "MODEL"}, requiredMode = Schema.RequiredMode.REQUIRED)
    private String role;

    @Schema(description = "Contenido textual del mensaje enviado o respondido", example = "¿Este enlace que recibí por SMS es seguro?", requiredMode = Schema.RequiredMode.REQUIRED)
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