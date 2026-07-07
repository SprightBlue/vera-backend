package com.unlam.verabackend.domain.model;

import com.unlam.verabackend.infrastructure.entity.User;
import lombok.*;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Notifications {
    private UUID id;
    private User user;
    private NotificationsType type;
    private String title;
    private String message;
    private Map<String, Object> payload;
    private boolean isRead;
    private LocalDateTime createdAt;
    private LocalDateTime readAt;
}