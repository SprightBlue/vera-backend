package com.unlam.verabackend.repositories;

import com.unlam.verabackend.entity.TrustInvitation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

// Repositorio solo para buscar el token de la invitacion
@Repository
public interface TrustInvitationRepository extends JpaRepository<TrustInvitation, Long> {
    
    Optional<TrustInvitation> findByToken(String token);
}