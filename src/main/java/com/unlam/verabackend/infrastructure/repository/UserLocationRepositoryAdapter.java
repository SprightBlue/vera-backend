package com.unlam.verabackend.infrastructure.repository;

import com.unlam.verabackend.domain.model.UserLocation;
import com.unlam.verabackend.domain.port.out.UserLocationRepository;
import com.unlam.verabackend.infrastructure.entity.UserLocationEntity;
import com.unlam.verabackend.infrastructure.entity.TrustContact;
import com.unlam.verabackend.infrastructure.mapper.UserLocationMapper;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class UserLocationRepositoryAdapter implements UserLocationRepository {

    private final JpaUserLocationRepository jpaRepository;
    private final UserLocationMapper mapper;
    private final EntityManager entityManager;

    @Override
    @Transactional
    public UserLocation save(UserLocation userLocation, Long trustContactId) {
        TrustContact trustContactProxy = entityManager.getReference(TrustContact.class, trustContactId);

        UserLocationEntity entity = mapper.toEntity(userLocation, trustContactProxy);

        UserLocationEntity savedEntity = jpaRepository.save(entity);
        entityManager.flush();

        return mapper.toDomain(savedEntity);
    }

    @Override
    public Optional<UserLocation> findByTrustContactId(Long trustContactId) {
        return jpaRepository.findByTrustContactId(trustContactId).map(mapper::toDomain);
    }
}