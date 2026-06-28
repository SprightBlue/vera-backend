package com.unlam.verabackend.infrastructure.mapper;

import com.unlam.verabackend.domain.model.UserLocation;
import com.unlam.verabackend.infrastructure.entity.UserLocationEntity;
import org.springframework.stereotype.Component;

@Component
public class UserLocationMapper {

    public UserLocation toDomain(UserLocationEntity entity) {
        if (entity == null) return null;
        return UserLocation.builder()
                .id(entity.getId())
                .trustContact(entity.getTrustContact())
                .latitude(entity.getLatitude())
                .longitude(entity.getLongitude())
                .locationText(entity.getLocationText())
                .isConnected(entity.isConnected())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public UserLocationEntity toEntity(UserLocation domain) {
        if (domain == null) return null;
        return UserLocationEntity.builder()
                .id(domain.getId())
                .trustContact(domain.getTrustContact())
                .latitude(domain.getLatitude())
                .longitude(domain.getLongitude())
                .locationText(domain.getLocationText())
                .isConnected(domain.isConnected())
                .build();
    }
}