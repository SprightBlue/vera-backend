package com.unlam.verabackend.infrastructure.repository;

import com.unlam.verabackend.infrastructure.entity.UserLocationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface JpaUserLocationRepository extends JpaRepository<UserLocationEntity, UUID> {
    Optional<UserLocationEntity> findByTrustContactId(Long trustContactId);

    @Query("SELECT ul FROM UserLocationEntity ul WHERE ul.trustContact.protectedUser.email = :email")
    Optional<UserLocationEntity> findByProtectedUserEmail(@Param("email") String email);
}