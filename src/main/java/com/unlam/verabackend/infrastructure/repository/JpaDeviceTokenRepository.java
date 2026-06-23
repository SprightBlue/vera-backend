package com.unlam.verabackend.infrastructure.repository;

import com.unlam.verabackend.infrastructure.entity.DeviceTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface JpaDeviceTokenRepository extends JpaRepository<DeviceTokenEntity, UUID> {
    Optional<DeviceTokenEntity> findByToken(String token);
    List<DeviceTokenEntity> findByUserEmailAndActiveTrue(String email);
}
