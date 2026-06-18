package com.unlam.verabackend.infrastructure.repository;
import org.springframework.stereotype.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import com.unlam.verabackend.infrastructure.entity.TrustContact;

@Repository
public interface TrustContactRepository extends JpaRepository<TrustContact, Long> {

    List<TrustContact> findByCarerId(Long carerId); 

    List<TrustContact> findByProtectedUserId(Long protectedUserId);

    boolean existsByCarerIdAndProtectedUser_Id(Long carerId, Long protectedUserId);

    List<TrustContact> findByReceiveAlertSummariesTrue();
}
