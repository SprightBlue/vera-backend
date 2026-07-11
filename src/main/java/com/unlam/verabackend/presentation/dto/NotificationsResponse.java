package com.unlam.verabackend.presentation.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.unlam.verabackend.domain.model.Notifications;
import com.unlam.verabackend.domain.model.NotificationsType;
import com.unlam.verabackend.presentation.utils.DateFormatter;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "DTO que representa una notificación enviada automáticamente ante eventos del ecosistema")
public class NotificationsResponse {

    @Schema(description = "Identificador único de la notificación (UUID)", example = "7b5a1e2f-3d4c-5b6a-7e8f-9a0b1c2d3e4f", requiredMode = Schema.RequiredMode.REQUIRED)
    private UUID id;

    @Schema(description = "Tipo de evento que originó la notificación", example = "ALERT", requiredMode = Schema.RequiredMode.REQUIRED)
    private NotificationsType type;

    @Schema(description = "Título de la notificación", example = "Nueva alerta crítica", requiredMode = Schema.RequiredMode.REQUIRED)
    private String title;

    @Schema(description = "Cuerpo del mensaje descriptivo de la notificación", example = "Tu protegido María García ha activado una alerta de riesgo alto.", requiredMode = Schema.RequiredMode.REQUIRED)
    private String message;

    @Schema(description = "Metadatos dinámicos adjuntos a la notificación (ej. alertId, trustContactId)", example = "{\"alertId\": \"4a2b1c3d-5e6f-7a8b-9c0d-1e2f3a4b5c6d\"}")
    private Map<String, Object> payload;

    @JsonProperty("isRead")
    @Schema(description = "Estado de lectura por parte del usuario", example = "false", requiredMode = Schema.RequiredMode.REQUIRED)
    private boolean isRead;

    @Schema(description = "Fecha en formato relativo en la que se leyó la notificación. Nulo si no fue leída.", example = "Hace 2 minutos")
    private String readAt;

    @Schema(description = "Fecha de emisión automática de la notificación en formato relativo", example = "Hace 1 hora", requiredMode = Schema.RequiredMode.REQUIRED)
    private String createdAt;

    public static NotificationsResponse fromDomain(Notifications domain) {
        if (domain == null) return null;

        return NotificationsResponse.builder()
                .id(domain.getId())
                .type(domain.getType())
                .title(domain.getTitle())
                .message(domain.getMessage())
                .payload(domain.getPayload())
                .isRead(domain.isRead())
                .readAt(DateFormatter.formatRelativeDate(domain.getReadAt()))
                .createdAt(DateFormatter.formatRelativeDate(domain.getCreatedAt()))
                .build();
    }
}