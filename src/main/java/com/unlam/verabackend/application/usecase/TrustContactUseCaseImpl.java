package com.unlam.verabackend.application.usecase;

import com.unlam.verabackend.domain.model.NotificationsType;
import com.unlam.verabackend.domain.port.in.TrustContactUseCase;
import com.unlam.verabackend.infrastructure.repository.UserRepository;
import com.unlam.verabackend.domain.model.InvitationStatus;
import com.unlam.verabackend.domain.model.SensitivityLevel;
import com.unlam.verabackend.infrastructure.entity.TrustContact;
import com.unlam.verabackend.infrastructure.entity.TrustInvitation;
import com.unlam.verabackend.presentation.dto.GenerateInvitationRequest;
import com.unlam.verabackend.presentation.dto.GenerateInvitationResponse;
import com.unlam.verabackend.presentation.dto.InvitationDetailsResponse;
import com.unlam.verabackend.presentation.dto.ProtectedPersonResponse;
import com.unlam.verabackend.infrastructure.entity.User;
import com.unlam.verabackend.infrastructure.repository.TrustContactRepository;
import com.unlam.verabackend.infrastructure.repository.TrustInvitationRepository;
import com.unlam.verabackend.application.service.NotificationService;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TrustContactUseCaseImpl implements TrustContactUseCase {

    private final TrustContactRepository trustContactRepository;
    private final UserRepository userRepository;
    private final TrustInvitationRepository trustInvitationRepository;
    private final NotificationService notificationService;

    @Value("${app.frontend.url:http://localhost:5173}")
    private String frontendUrl;

    @Override
    @Transactional
    public GenerateInvitationResponse generateInvitationLink(GenerateInvitationRequest request, String carerEmail) {
        if (carerEmail.equalsIgnoreCase(request.getEmail())) {
            throw new IllegalArgumentException("No puedes generar una invitación para ti mismo");
        }

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
                .expiresAt(LocalDateTime.now().plusDays(7))
                .status(InvitationStatus.PENDING)
                .build();

        TrustInvitation savedInvitation = trustInvitationRepository.save(invitation);

        userRepository.findByEmail(request.getEmail()).ifPresent(invitedUser -> {
            Map<String, Object> payload = Map.of(
                    "invitationId", savedInvitation.getId().toString(),
                    "carerName", carer.getFullName()
            );

            notificationService.createAndSendNotification(
                    invitedUser,
                    NotificationsType.INVITATION,
                    carer.getFullName(),
                    payload
            );
        });

        String fullLink = frontendUrl + "/invite/" + uniqueToken;
        return new GenerateInvitationResponse(uniqueToken, fullLink);
    }

    @Override
    @Transactional(readOnly = true)
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

        if (invitation.getStatus() != InvitationStatus.PENDING) {
            throw new RuntimeException("Esta invitación ya fue utilizada o aceptada");
        }

        if (invitation.getExpiresAt() != null && invitation.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("El enlace de invitación ha expirado");
        }

        return new InvitationDetailsResponse(
                invitation.getId(),
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

        if (invitation.getExpiresAt() != null && invitation.getExpiresAt().isBefore(LocalDateTime.now())) {
            invitation.setStatus(InvitationStatus.EXPIRED);
            trustInvitationRepository.save(invitation);
            throw new RuntimeException("El link de invitación expiró.");
        }

        User protectedUser = userRepository.findByEmail(protectedUserEmail)
                .orElseThrow(() -> new RuntimeException("Usuario protegido no encontrado"));

        if (invitation.getCarer().getId().equals(protectedUser.getId())) {
            throw new RuntimeException("No podés aceptar tu propia invitación de seguridad");
        }

        if (trustContactRepository.existsByCarerIdAndProtectedUser_Id(invitation.getCarer().getId(), protectedUser.getId())) {
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

    // --- MÉTODOS DE BANDEJA OPERADOS POR EL EMAIL DEL PROTEGIDO (AUTH) ---

    @Override
    @Transactional(readOnly = true)
    public List<InvitationDetailsResponse> getPendingInvitationsForMe(String myEmail) {
        List<TrustInvitation> pendingInvitations = trustInvitationRepository.findByEmailAndStatus(myEmail, InvitationStatus.PENDING);

        return pendingInvitations.stream()
                .filter(inv -> inv.getExpiresAt() == null || inv.getExpiresAt().isAfter(LocalDateTime.now()))
                .map(inv -> new InvitationDetailsResponse(
                        inv.getId(),
                        inv.getFullName(),
                        inv.getCarer().getFullName(),
                        inv.getRelationship()
                ))
                .toList();
    }

    @Override
    @Transactional
    public void acceptInvitationById(Long invitationId, String protectedUserEmail) {
        TrustInvitation invitation = trustInvitationRepository.findById(invitationId)
                .orElseThrow(() -> new RuntimeException("Invitación no encontrada"));

        if (invitation.getStatus() != InvitationStatus.PENDING) {
            throw new RuntimeException("Esta invitación ya fue procesada");
        }

        if (!invitation.getEmail().equalsIgnoreCase(protectedUserEmail)) {
            throw new RuntimeException("No tenés permiso para operar esta invitación");
        }

        User protectedUser = userRepository.findByEmail(protectedUserEmail)
                .orElseThrow(() -> new RuntimeException("Usuario protegido no encontrado"));

        if (trustContactRepository.existsByCarerIdAndProtectedUser_Id(invitation.getCarer().getId(), protectedUser.getId())) {
            invitation.setStatus(InvitationStatus.ACCEPTED);
            trustInvitationRepository.save(invitation);
            throw new RuntimeException("Ya existe una relación activa con este usuario");
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

        notificationService.createAndSendNotification(
                invitation.getCarer(),
                NotificationsType.INVITATION_ACCEPTED,
                protectedUser.getFullName(),
                Map.of("invitationId", invitation.getId().toString())
        );
    }

    @Override
    @Transactional
    public void rejectInvitationById(Long invitationId, String protectedUserEmail) {
        TrustInvitation invitation = trustInvitationRepository.findById(invitationId)
                .orElseThrow(() -> new RuntimeException("Invitación no encontrada"));

        if (invitation.getStatus() != InvitationStatus.PENDING) {
            throw new RuntimeException("Esta invitación ya fue procesada");
        }

        if (!invitation.getEmail().equalsIgnoreCase(protectedUserEmail)) {
            throw new RuntimeException("No tenés permiso para rechazar esta invitación");
        }

        invitation.setStatus(InvitationStatus.REJECTED);
        trustInvitationRepository.save(invitation);

        notificationService.createAndSendNotification(
                invitation.getCarer(),
                NotificationsType.INVITATION_REJECTED,
                protectedUserEmail,
                Map.of("invitationId", invitation.getId().toString())
        );
    }

    @Override
    @Transactional
    public void deleteProtectedPerson(Long id) {
        if (trustInvitationRepository.existsById(id)) {
            trustInvitationRepository.deleteById(id);
        }
        if (trustContactRepository.existsById(id)) {
            trustContactRepository.deleteById(id);
        }
    }

    @Override
    @Transactional
    public void updateConfiguration(Long id, String sensitivityLevelStr, Boolean notifyHighRisk) {
        SensitivityLevel sensitivityEnum = null;
        if (sensitivityLevelStr != null) {
            sensitivityEnum = SensitivityLevel.valueOf(sensitivityLevelStr.toUpperCase());
        }

        final SensitivityLevel finalSensitivity = sensitivityEnum;

        trustContactRepository.findById(id).ifPresent(contact -> {
            if (finalSensitivity != null) contact.setSensitivityLevel(finalSensitivity);
            if (notifyHighRisk != null) contact.setNotifyHighRisk(notifyHighRisk);
            trustContactRepository.save(contact);
        });

        trustInvitationRepository.findById(id).ifPresent(invitation -> {
            if (finalSensitivity != null) invitation.setSensitivityLevel(finalSensitivity);
            if (notifyHighRisk != null) invitation.setNotifyHighRisk(notifyHighRisk);
            trustInvitationRepository.save(invitation);
        });
    }
}
