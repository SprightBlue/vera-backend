package com.unlam.verabackend.infrastructure.repository.jpa;

import com.unlam.verabackend.infrastructure.entity.UserLocationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface JpaUserLocationRepository extends JpaRepository<UserLocationEntity, UUID> {
    @Query("""
       SELECT ul FROM UserLocationEntity ul
       LEFT JOIN FETCH ul.trustContact tc
       LEFT JOIN FETCH tc.carer c
       WHERE tc.protectedUser.email = :email
    """)
    Optional<UserLocationEntity> findByProtectedUserEmail(@Param("email") String email);
    @Query("""
       SELECT ul FROM UserLocationEntity ul
       LEFT JOIN FETCH ul.trustContact tc
       LEFT JOIN FETCH tc.carer c
       WHERE tc.carer.email = :carerEmail
       ORDER BY ul.updatedAt DESC
    """)
    List<UserLocationEntity> findTop3ByCarerEmailOrderByUpdatedAtDesc(@Param("carerEmail") String carerEmail);
    @Query("""
       SELECT COUNT(ul) FROM UserLocationEntity ul
       WHERE ul.trustContact.carer.email = :carerEmail
         AND ul.isConnected = true
    """)
    long countConnectedUsersByCarerEmail(@Param("carerEmail") String carerEmail);
}