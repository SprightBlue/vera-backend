package com.unlam.verabackend.repositories;

import org.springframework.stereotype.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import com.unlam.verabackend.entity.TrustContact;

@Repository
public interface TrustContactRepository extends JpaRepository<TrustContact, Long> {

    List<TrustContact> findByCarerId(Long carerId); 

    List<TrustContact> findByProtectedUserId(Long protectedUserId);

    boolean existsByCarerIdAndProtectedUserId(Long carerId, Long protectedUserId);

    
}
