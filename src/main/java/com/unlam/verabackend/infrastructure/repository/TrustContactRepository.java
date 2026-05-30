package com.unlam.verabackend.infrastructure.repository;

import org.springframework.stereotype.Repository;

import com.unlam.verabackend.infrastructure.entity.TrustContact;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

@Repository
public interface TrustContactRepository extends JpaRepository<TrustContact, Long> {

    List<TrustContact> findByCarerId(Long carerId); 

    List<TrustContact> findByProtectedUserId(Long protectedUserId);

    boolean existsByCarerIdAndProtectedUserId(Long carerId, Long protectedUserId);

    
}
