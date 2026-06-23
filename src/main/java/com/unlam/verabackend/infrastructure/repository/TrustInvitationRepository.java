package com.unlam.verabackend.infrastructure.repository;
import com.unlam.verabackend.domain.model.InvitationStatus;
import com.unlam.verabackend.infrastructure.entity.TrustInvitation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TrustInvitationRepository extends JpaRepository<TrustInvitation, Long> {
    
    Optional<TrustInvitation> findByToken(String token);

    List<TrustInvitation> findByCarerIdAndStatus(Long carerId, InvitationStatus status);

    List<TrustInvitation> findByProtectedPersonIdAndStatus(Long protectedPersonId, InvitationStatus status);

    List<TrustInvitation> findByEmailAndStatus(String email, InvitationStatus status);
}