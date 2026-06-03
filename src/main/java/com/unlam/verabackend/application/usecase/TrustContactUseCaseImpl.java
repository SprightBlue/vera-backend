package com.unlam.verabackend.application.usecase;


import com.unlam.verabackend.domain.repository.UserRepository;
import com.unlam.verabackend.entity.InvitationStatus;
import com.unlam.verabackend.entity.TrustContact;
import com.unlam.verabackend.entity.TrustInvitation;
import com.unlam.verabackend.presentation.dto.GenerateInvitationRequest;
import com.unlam.verabackend.presentation.dto.GenerateInvitationResponse;
import com.unlam.verabackend.presentation.dto.InvitationDetailsResponse;
import com.unlam.verabackend.presentation.dto.ProtectedPersonResponse;
import com.unlam.verabackend.infrastructure.entity.User;
import com.unlam.verabackend.infrastructure.repository.TrustContactRepository;
import com.unlam.verabackend.infrastructure.repository.TrustInvitationRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TrustContactUseCaseImpl implements TrustContactUseCase {
    
    private final TrustContactRepository trustContactRepository;
    private final UserRepository userRepository;

    private final TrustInvitationRepository trustInvitationRepository;
    
    @Value("${app.frontend.url:http://localhost:5173}")
    private String frontendUrl;
    
    @Override
    @Transactional
    public GenerateInvitationResponse generateInvitationLink(GenerateInvitationRequest request, String carerEmail) {
        
        User carer = userRepository.findByEmail(carerEmail)
                .orElseThrow(() -> new RuntimeException("Usuario cuidador no encontrado"));

        String uniqueToken = UUID.randomUUID().toString();

        TrustInvitation invitation = TrustInvitation.builder()
            .carer(carer)
            .token(uniqueToken)
            .fullName(request.getFullName())    
            .contactNumber(request.getContactNumber()) 
            .email(request.getEmail())                
            .relationship(request.getRelationship())
            .sensitivityLevel(request.getSensitivityLevel())
            .notifyHighRisk(request.isNotifyHighRisk())
            .receiveAlertSummaries(request.isReceiveAlertSummaries())
            .allowBasicConfig(false)
            .monitorWhatsapp(false)
            .monitorSms(false)
            .monitorGmail(false)
            .monitorTelegram(false)
            .build();
        trustInvitationRepository.save(invitation);

        String fullLink = frontendUrl + "/invite/" + uniqueToken;
        
        return new GenerateInvitationResponse
        (uniqueToken, fullLink);
    }


    @Override
    public List<ProtectedPersonResponse> getMyProtectedPeople(String carerEmail) {
        User carer = userRepository.findByEmail(carerEmail)
                .orElseThrow(() -> new RuntimeException("Cuidador no encontrado"));

        List<ProtectedPersonResponse> responseList = new ArrayList<>();

        List<TrustContact> activeContacts = trustContactRepository.findByCarerId(carer.getId());
        for (TrustContact contact : activeContacts) {
            responseList.add(ProtectedPersonResponse.builder()
                    .id(contact.getId())
                    .fullName(contact.getProtectedUser().getFullName())
                    .email(contact.getProtectedUser().getEmail())
                    .relationship(contact.getRelationship())
                    .status("ACTIVE") 
                    .build());
        }

        List<TrustInvitation> pendingInvitations = trustInvitationRepository.findByCarerIdAndStatus(
                carer.getId(), InvitationStatus.PENDING);
        
        for (TrustInvitation inv : pendingInvitations) {
            responseList.add(ProtectedPersonResponse.builder()
                    .id(inv.getId()) 
                    .fullName(inv.getFullName())
                    .email(inv.getEmail())
                    .contactNumber(inv.getContactNumber())
                    .relationship(inv.getRelationship())
                    .status("PENDING") 
                    .build());
        }

        return responseList;
    }


    @Override
    @Transactional(readOnly = true)
    public InvitationDetailsResponse getInvitationDetails(String token) {
        
        TrustInvitation invitation = trustInvitationRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("El enlace de invitación no es válido o ya no existe"));

        if (invitation.getStatus() != com.unlam.verabackend.entity.InvitationStatus.PENDING) {
            throw new RuntimeException("Esta invitación ya fue utilizada o aceptada");
        }

        if (invitation.getExpiresAt().isBefore(java.time.LocalDateTime.now())) {
            throw new RuntimeException("El enlace de invitación ha expirado");
        }

        return new InvitationDetailsResponse(
                invitation.getFullName(),
                invitation.getCarer().getFullName(),
                invitation.getRelationship()
        );
    }


    @Override
    @Transactional
    public void acceptInvitation(String token, String protectedUserEmail) {
        TrustInvitation invitation = trustInvitationRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Invitación no encontrada o token inválido"));

        if (invitation.getStatus() != InvitationStatus.PENDING) {
            throw new RuntimeException("Esta invitación ya fue procesada anteriormente");
        }
        
        if (invitation.getExpiresAt().isBefore(LocalDateTime.now())) {
            invitation.setStatus(InvitationStatus.EXPIRED);
            trustInvitationRepository.save(invitation);
            throw new RuntimeException("El link de invitación expiró. Pedile a tu cuidador que genere uno nuevo.");
        }

        User protectedUser = userRepository.findByEmail(protectedUserEmail)
                .orElseThrow(() -> new RuntimeException("Usuario protegido no encontrado"));

        if (invitation.getCarer().getId().equals(protectedUser.getId())) {
            throw new RuntimeException("No podés aceptar tu propia invitación de seguridad");
        }

        if (trustContactRepository.existsByCarerIdAndProtectedUserId(invitation.getCarer().getId(), protectedUser.getId())) {
            throw new RuntimeException("Ya estás siendo protegido por este usuario");
        }

        TrustContact newContact = TrustContact.builder()
                .carer(invitation.getCarer())
                .protectedUser(protectedUser)
                .relationship(invitation.getRelationship())
                .sensitivityLevel(invitation.getSensitivityLevel())
                .notifyHighRisk(invitation.isNotifyHighRisk())
                .receiveAlertSummaries(invitation.isReceiveAlertSummaries())
                .build();

        trustContactRepository.save(newContact);

        invitation.setStatus(InvitationStatus.ACCEPTED);
        trustInvitationRepository.save(invitation);
    }

    public void deleteProtectedPerson(Long id) {

        if (trustInvitationRepository.existsById(id)) {
            trustInvitationRepository.deleteById(id);
        }
        
        if (trustContactRepository.existsById(id)) {
        trustContactRepository.deleteById(id);
        } else {
            System.out.println("❌ No se encontró el ID " + id + " en ninguna tabla");
        }

}


}
