package com.unlam.verabackend.presentation.dto;

import com.unlam.verabackend.domain.model.Notifications;
import com.unlam.verabackend.domain.model.NotificationsType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
public class NotificationsResponse {
    private UUID id;
    private NotificationsType type;
    private String title;
    private String message;
    private Map<String, Object> payload;
    private boolean isRead;
    private LocalDateTime createdAt;

    public static NotificationsResponse fromDomain(Notifications domain) {
        if (domain == null) return null;

        return NotificationsResponse.builder()
                .id(domain.getId())
                .type(domain.getType())
                .title(domain.getTitle())
                .message(domain.getMessage())
                .payload(domain.getPayload())
                .isRead(domain.isRead())
                .createdAt(domain.getCreatedAt())
                .build();
    }
}