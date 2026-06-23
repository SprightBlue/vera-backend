package com.unlam.verabackend.infrastructure.mapper;

import com.unlam.verabackend.domain.model.Notifications;
import com.unlam.verabackend.infrastructure.entity.NotificationsEntity;
import com.unlam.verabackend.infrastructure.entity.User;
import org.springframework.stereotype.Component;

@Component
public class NotificationMapper {

    public Notifications toDomain(NotificationsEntity entity) {
        if (entity == null) return null;

        return Notifications.builder()
                .id(entity.getId())
                .user(entity.getUser())
                .type(entity.getType())
                .title(entity.getTitle())
                .message(entity.getMessage())
                .payload(entity.getPayload())
                .isRead(entity.isRead())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    public NotificationsEntity toEntity(Notifications domain, User userEntity) {
        if (domain == null) return null;

        return NotificationsEntity.builder()
                .id(domain.getId())
                .user(userEntity)
                .type(domain.getType())
                .title(domain.getTitle())
                .message(domain.getMessage())
                .payload(domain.getPayload())
                .isRead(domain.isRead())
                .createdAt(domain.getCreatedAt())
                .build();
    }
}