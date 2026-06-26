package com.unlam.verabackend.infrastructure.repository;

import com.unlam.verabackend.domain.model.UserLocation;
import com.unlam.verabackend.domain.port.out.UserLocationRepository;
import com.unlam.verabackend.infrastructure.entity.UserLocationEntity;
import com.unlam.verabackend.infrastructure.mapper.UserLocationMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class UserLocationRepositoryAdapter implements UserLocationRepository {

    private final JpaUserLocationRepository jpaRepository;
    private final UserLocationMapper mapper;

    @Override
    public UserLocation save(UserLocation userLocation) {
        UserLocationEntity entity = mapper.toEntity(userLocation);
        UserLocationEntity savedEntity = jpaRepository.save(entity);
        return mapper.toDomain(savedEntity);
    }

    @Override
    public Optional<UserLocation> findById(UUID id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Optional<UserLocation> findByTrustContactId(Long trustContactId) {
        return jpaRepository.findByTrustContactId(trustContactId).map(mapper::toDomain);
    }

    @Override
    public Optional<UserLocation> findByProtectedUserEmail(String email) {
        return jpaRepository.findByProtectedUserEmail(email).map(mapper::toDomain);
    }
}