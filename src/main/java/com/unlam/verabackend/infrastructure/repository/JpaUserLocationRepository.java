package com.unlam.verabackend.infrastructure.repository;

import com.unlam.verabackend.infrastructure.entity.UserLocationEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface JpaUserLocationRepository extends JpaRepository<UserLocationEntity, UUID> {
    @EntityGraph(attributePaths = {"trustContact", "trustContact.protectedUser"})
    Optional<UserLocationEntity> findByTrustContactId(Long trustContactId);
}