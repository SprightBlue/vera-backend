package com.unlam.verabackend.presentation.dto;

import com.unlam.verabackend.domain.model.Chats;
import com.unlam.verabackend.presentation.utils.DateFormatter;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
@Schema(description = "DTO que representa un resumen de la sesión de chat con Inteligencia Artificial")
public class ChatSessionResponse {

    @Schema(description = "Identificador único de la sesión de chat (UUID)", example = "9b1deb4d-3b7d-4bad-9bdd-2b0d7b3dcb6d", requiredMode = Schema.RequiredMode.REQUIRED)
    private UUID id;

    @Schema(description = "Título o asunto de la conversación generado automáticamente", example = "Consulta sobre correo de phishing bancario", requiredMode = Schema.RequiredMode.REQUIRED)
    private String title;

    @Schema(description = "Fecha y hora de la última interacción/actualización del chat en formato relativo", example = "Hace 2 horas", requiredMode = Schema.RequiredMode.REQUIRED)
    private String updatedAt;

    public static ChatSessionResponse fromDomain(Chats chat) {
        if (chat == null) {
            return null;
        }
        return ChatSessionResponse.builder()
                .id(chat.getId())
                .title(chat.getTitle() != null ? chat.getTitle() : null)
                .updatedAt(DateFormatter.formatRelativeDate(chat.getUpdatedAt()))
                .build();
    }
}