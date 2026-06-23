package com.unlam.verabackend.infrastructure.repository;

import com.unlam.verabackend.domain.model.DeviceToken;
import com.unlam.verabackend.domain.port.out.DeviceTokenRepository;
import com.unlam.verabackend.infrastructure.entity.DeviceTokenEntity;
import com.unlam.verabackend.infrastructure.entity.User;
import com.unlam.verabackend.infrastructure.mapper.DeviceTokenMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class DeviceTokenRepositoryAdapter implements DeviceTokenRepository {

    private final JpaDeviceTokenRepository jpaRepository;
    private final DeviceTokenMapper mapper;

    @Override
    public DeviceToken saveOrUpdate(User user, String token, String platform) {
        DeviceTokenEntity entity = jpaRepository.findByToken(token)
                .orElseGet(DeviceTokenEntity::new);

        entity.setUser(user);
        entity.setToken(token);
        entity.setPlatform(platform);
        entity.setActive(true);

        return mapper.toDomain(jpaRepository.save(entity));
    }

    @Override
    public List<DeviceToken> findActiveByUserEmail(String email) {
        return jpaRepository.findByUserEmailAndActiveTrue(email)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public void deactivateToken(String token) {
        jpaRepository.findByToken(token).ifPresent(entity -> {
            entity.setActive(false);
            jpaRepository.save(entity);
        });
    }
}
