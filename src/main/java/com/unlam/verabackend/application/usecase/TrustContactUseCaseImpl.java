package com.unlam.verabackend.application.usecase;

import com.unlam.verabackend.infrastructure.provider.CloudinaryFileCloudAdapter;
import com.unlam.verabackend.domain.exception.ResourceNotFoundException;
import com.unlam.verabackend.domain.model.NotificationsType;
import com.unlam.verabackend.domain.port.in.TrustContactUseCase;
import com.unlam.verabackend.infrastructure.repository.UserRepository;
import com.unlam.verabackend.domain.model.InvitationStatus;
import com.unlam.verabackend.domain.model.SensitivityLevel;
import com.unlam.verabackend.infrastructure.entity.TrustContact;
import com.unlam.verabackend.infrastructure.entity.TrustInvitation;
import com.unlam.verabackend.presentation.dto.*;
import com.unlam.verabackend.infrastructure.entity.User;
import com.unlam.verabackend.infrastructure.repository.TrustContactRepository;
import com.unlam.verabackend.infrastructure.repository.TrustInvitationRepository;
import com.unlam.verabackend.application.service.NotificationService;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class TrustContactUseCaseImpl implements TrustContactUseCase {

    private final TrustContactRepository trustContactRepository;
    private final UserRepository userRepository;
    private final TrustInvitationRepository trustInvitationRepository;
    private final NotificationService notificationService;
    private final CloudinaryFileCloudAdapter cloudinaryFileCloudAdapter;

    @Value("${app.frontend.url:http://localhost:5173}")
    private String frontendUrl;

    @Override
    @Transactional
    public GenerateInvitationResponse generateInvitationLink(GenerateInvitationRequest request, String carerEmail) {
        if (carerEmail.equalsIgnoreCase(request.getEmail())) {
            throw new IllegalArgumentException("No puedes generar una invitación para ti mismo");
        }

        User carer = userRepository.findByEmail(carerEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario cuidador no encontrado"));

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
                    "id", savedInvitation.getId(),
                    "fullName", savedInvitation.getFullName(),
                    "caregiverName", carer.getFullName(),
                    "relationship", savedInvitation.getRelationship()
            );

            notificationService.createAndDispatch(
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
            responseList.add(buildActivePersonResponse(contact));
        }

        List<TrustInvitation> pendingInvitations = trustInvitationRepository.findByCarerIdAndStatus(
                carer.getId(), InvitationStatus.PENDING);
        for (TrustInvitation inv : pendingInvitations) {
            if (inv.getProtectedPerson() != null) continue;
            responseList.add(buildPendingPersonResponse(inv));
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

        TrustContact newContact = buildContactFromInvitation(invitation, protectedUser);
        trustContactRepository.save(newContact);

        invitation.setStatus(InvitationStatus.ACCEPTED);
        trustInvitationRepository.save(invitation);
    }

    private TrustContact buildContactFromInvitation(TrustInvitation inv, User protectedUser) {
        boolean hasProtectedPerson = inv.getProtectedPerson() != null;
        return TrustContact.builder()
                .carer(hasProtectedPerson ? protectedUser : inv.getCarer())
                .protectedUser(hasProtectedPerson ? inv.getProtectedPerson() : protectedUser)
                .relationship(inv.getRelationship())
                .sensitivityLevel(inv.getSensitivityLevel())
                .notifyHighRisk(inv.isNotifyHighRisk())
                .receiveAlertSummaries(inv.isReceiveAlertSummaries())
                .build();
    }

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
            throw new IllegalStateException("Ya existe una relación de cuidado activa con este usuario.");
        }

        TrustContact newContact = buildContactFromInvitation(invitation, protectedUser);
        trustContactRepository.save(newContact);

        invitation.setStatus(InvitationStatus.ACCEPTED);
        trustInvitationRepository.save(invitation);

        notificationService.createAndDispatch(
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

        notificationService.createAndDispatch(
                invitation.getCarer(),
                NotificationsType.INVITATION_REJECTED,
                protectedUserEmail,
                Map.of("invitationId", invitation.getId().toString())
        );
    }

    @Override
    @Transactional
    public void deleteProtectedPerson(Long id, String status, String carerEmail) {
        User carer = userRepository.findByEmail(carerEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Cuidador no encontrado"));

        if ("ACTIVE".equalsIgnoreCase(status)) {
            TrustContact contact = trustContactRepository.findById(id)
                    .filter(c -> c.getCarer().getId().equals(carer.getId()))
                    .orElseThrow(() -> new ResourceNotFoundException("Protegido activo no encontrado"));
            trustContactRepository.delete(contact);
            return;
        }

        if ("PENDING".equalsIgnoreCase(status)) {
            TrustInvitation invitation = trustInvitationRepository.findById(id)
                    .filter(inv -> inv.getCarer().getId().equals(carer.getId()))
                    .filter(inv -> inv.getStatus() == InvitationStatus.PENDING)
                    .orElseThrow(() -> new ResourceNotFoundException("Invitación pendiente no encontrada"));
            trustInvitationRepository.delete(invitation);
            return;
        }

        throw new IllegalArgumentException("Status inválido: " + status);
    }

    @Override
    @Transactional
    public void updateConfiguration(Long id, String sensitivityLevelStr, Boolean notifyHighRisk) {
        SensitivityLevel sensitivityEnum = null;
        if (sensitivityLevelStr != null) {
            sensitivityEnum = SensitivityLevel.valueOf(sensitivityLevelStr.toUpperCase());
        }

        final SensitivityLevel finalSensitivity = sensitivityEnum;

        TrustContact contact = trustContactRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Contacto de confianza no encontrado"));

        if (finalSensitivity != null) {
            contact.setSensitivityLevel(finalSensitivity);
        }
        if (notifyHighRisk != null) {
            contact.setNotifyHighRisk(notifyHighRisk);
        }
        
        trustContactRepository.save(contact);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CarerResponse> getMyCarers(String protectedUserEmail) {
        User protectedUser = userRepository.findByEmail(protectedUserEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        List<TrustContact> carers = trustContactRepository.findByProtectedUserId(protectedUser.getId());

        return carers.stream()
                .map(contact -> CarerResponse.builder()
                        .contactId(contact.getId())
                        .fullName(contact.getCarer().getFullName())
                        .email(contact.getCarer().getEmail())
                        .image(contact.getCarer().getImage())
                        .relationship(contact.getRelationship())
                        .sensitivityLevel(contact.getSensitivityLevel())
                        .notifyHighRisk(contact.isNotifyHighRisk())
                        .build())
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ProtectedPersonResponse getProtectedPersonById(Long id, String status, String carerEmail) {
        User carer = userRepository.findByEmail(carerEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Cuidador no encontrado"));

        if ("ACTIVE".equalsIgnoreCase(status)) {
            TrustContact contact = trustContactRepository.findById(id)
                    .filter(c -> c.getCarer().getId().equals(carer.getId()))
                    .orElseThrow(() -> new ResourceNotFoundException("Protegido activo no encontrado"));
            return buildActivePersonResponse(contact);
        }

        if ("PENDING".equalsIgnoreCase(status)) {
            TrustInvitation invitation = trustInvitationRepository.findById(id)
                    .filter(inv -> inv.getCarer().getId().equals(carer.getId()))
                    .filter(inv -> inv.getStatus() == InvitationStatus.PENDING)
                    .orElseThrow(() -> new ResourceNotFoundException("Invitación pendiente no encontrada"));
            return buildPendingPersonResponse(invitation);
        }

        throw new IllegalArgumentException("Status inválido: " + status);
    }

    @Override
    @Transactional
    public String uploadProtectedPersonImage(MultipartFile image) throws IOException {
        return cloudinaryFileCloudAdapter.uploadImage(image, "protected");
    }

    @Override
    @Transactional
    public ProtectedPersonResponse updateInformation(Long id, String status, String fullName, String relationship, String contactNumber, String image) {
        if ("ACTIVE".equalsIgnoreCase(status)) {
            TrustContact contact = trustContactRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Vínculo no encontrado"));
            User protectedUser = contact.getProtectedUser();
            protectedUser.setFullName(fullName);
            protectedUser.setPhone(contactNumber);
            protectedUser.setImage(StringUtils.hasText(image) ? image : "");
            contact.setRelationship(relationship);
            trustContactRepository.save(contact);
            return buildActivePersonResponse(contact);
        }

        if ("PENDING".equalsIgnoreCase(status)) {
            TrustInvitation protectedPerson = trustInvitationRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Protegido no encontrado"));

            protectedPerson.setFullName(fullName);
            protectedPerson.setRelationship(relationship);
            protectedPerson.setContactNumber(contactNumber);

            if (StringUtils.hasText(image)) {
                protectedPerson.setImage(image);
            } else {
                protectedPerson.setImage("");
            }

            trustInvitationRepository.save(protectedPerson);

            return buildPendingPersonResponse(protectedPerson);
        }

        throw new IllegalArgumentException("Status inválido: " + status);
    }

    private ProtectedPersonResponse buildActivePersonResponse(TrustContact contact) {
        User u = contact.getProtectedUser();
        return ProtectedPersonResponse.builder()
                .id(contact.getId())
                .protectedUserId(u.getId())
                .fullName(u.getFullName())
                .email(u.getEmail())
                .contactNumber(u.getPhone())
                .image(u.getImage())
                .relationship(contact.getRelationship())
                .sensitivityLevel(contact.getSensitivityLevel() != null ? contact.getSensitivityLevel().name() : null)
                .notifyHighRisk(contact.isNotifyHighRisk())
                .status("ACTIVE")
                .build();
    }

    private ProtectedPersonResponse buildPendingPersonResponse(TrustInvitation inv) {
        return ProtectedPersonResponse.builder()
                .id(inv.getId())
                .fullName(inv.getFullName())
                .email(inv.getEmail())
                .contactNumber(inv.getContactNumber())
                .relationship(inv.getRelationship())
                .image(inv.getImage())
                .status("PENDING")
                .build();
    }
} 
