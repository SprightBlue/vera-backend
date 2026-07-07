package com.unlam.verabackend.presentation.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.unlam.verabackend.domain.model.Notifications;
import com.unlam.verabackend.domain.model.NotificationsType;
import com.unlam.verabackend.presentation.utils.DateFormatter;
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
public class NotificationsResponse {
    private UUID id;
    private NotificationsType type;
    private String title;
    private String message;
    private Map<String, Object> payload;

    @JsonProperty("isRead")
    private boolean isRead;

    private String readAt;
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