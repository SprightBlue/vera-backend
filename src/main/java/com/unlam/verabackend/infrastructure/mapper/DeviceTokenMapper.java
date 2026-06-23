package com.unlam.verabackend.infrastructure.mapper;

import com.unlam.verabackend.domain.model.DeviceToken;
import com.unlam.verabackend.infrastructure.entity.DeviceTokenEntity;
import org.springframework.stereotype.Component;

@Component
public class DeviceTokenMapper {

    public DeviceToken toDomain(DeviceTokenEntity entity) {
        if (entity == null) return null;

        return DeviceToken.builder()
                .id(entity.getId())
                .user(entity.getUser())
                .token(entity.getToken())
                .platform(entity.getPlatform())
                .active(entity.isActive())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
