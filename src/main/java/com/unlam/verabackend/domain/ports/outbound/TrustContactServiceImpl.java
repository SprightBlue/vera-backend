package com.unlam.verabackend.domain.ports.outbound;

import com.unlam.verabackend.presentation.dto.request.GenerateInvitationRequest;
import com.unlam.verabackend.presentation.dto.response.GenerateInvitationResponse;
import com.unlam.verabackend.presentation.dto.response.InvitationDetailsResponse;
import com.unlam.verabackend.presentation.dto.response.ProtectedPersonResponse;
import com.unlam.verabackend.infrastructure.entity.InvitationStatus;
import com.unlam.verabackend.infrastructure.entity.TrustContact;
import com.unlam.verabackend.infrastructure.entity.TrustInvitation;
import com.unlam.verabackend.infrastructure.entity.User;

import com.unlam.verabackend.infrastructure.repository.TrustContactRepository;
import com.unlam.verabackend.infrastructure.repository.TrustInvitationRepository;
import com.unlam.verabackend.infrastructure.repository.UserRepository;
import com.unlam.verabackend.domain.ports.inbound.TrustContactService;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TrustContactServiceImpl implements TrustContactService {
    
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
                .contactInfo(request.getContactInfo())
                .relationship(request.getRelationship())
                .sensitivityLevel(request.getSensitivityLevel())
                .monitorWhatsapp(request.isMonitorWhatsapp())
                .monitorSms(request.isMonitorSms())
                .monitorGmail(request.isMonitorGmail())
                .monitorTelegram(request.isMonitorTelegram())
                .notifyHighRisk(request.isNotifyHighRisk())
                .receiveAlertSummaries(request.isReceiveAlertSummaries())
                .allowBasicConfig(request.isAllowBasicConfig())
                .build();

        trustInvitationRepository.save(invitation);

        String fullLink = frontendUrl + "/invite/" + uniqueToken;
        
        return new GenerateInvitationResponse
        (uniqueToken, fullLink);
    }


    @Override
    @Transactional(readOnly = true)
    public List<ProtectedPersonResponse> getMyProtectedPeople(String carerEmail) {
       
       
        User carer = userRepository.findByEmail(carerEmail)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        List<TrustContact> relations = trustContactRepository.findByCarerId(carer.getId());

        return relations.stream()
                .map(relation -> new ProtectedPersonResponse(
                        relation.getProtectedUser().getId(),
                        relation.getProtectedUser().getFullName(),
                        relation.getProtectedUser().getEmail()
                ))
                .collect(Collectors.toList());
    }


    @Override
    @Transactional(readOnly = true)
    public InvitationDetailsResponse getInvitationDetails(String token) {
        
        TrustInvitation invitation = trustInvitationRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("El enlace de invitación no es válido o ya no existe"));

        if (invitation.getStatus() != com.unlam.verabackend.infrastructure.entity.InvitationStatus.PENDING) {
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
                .monitorWhatsapp(invitation.isMonitorWhatsapp())
                .monitorSms(invitation.isMonitorSms())
                .monitorGmail(invitation.isMonitorGmail())
                .monitorTelegram(invitation.isMonitorTelegram())
                .notifyHighRisk(invitation.isNotifyHighRisk())
                .receiveAlertSummaries(invitation.isReceiveAlertSummaries())
                .allowBasicConfig(invitation.isAllowBasicConfig())
                .build();

        trustContactRepository.save(newContact);

        invitation.setStatus(InvitationStatus.ACCEPTED);
        trustInvitationRepository.save(invitation);
    }
    
}
