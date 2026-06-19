package com.unlam.verabackend.application.usecase;

import com.unlam.verabackend.application.service.SseService;
import com.unlam.verabackend.domain.model.InvitationStatus;
import com.unlam.verabackend.domain.model.NotificationsType;
import com.unlam.verabackend.domain.model.SensitivityLevel;
import com.unlam.verabackend.domain.port.in.ManageContactsUseCase;
import com.unlam.verabackend.infrastructure.repository.UserRepository;
import com.unlam.verabackend.infrastructure.entity.TrustContact;
import com.unlam.verabackend.infrastructure.entity.TrustInvitation;
import com.unlam.verabackend.infrastructure.entity.User;
import com.unlam.verabackend.infrastructure.repository.TrustContactRepository;
import com.unlam.verabackend.infrastructure.repository.TrustInvitationRepository;
import com.unlam.verabackend.presentation.dto.AddContactRequest;
import com.unlam.verabackend.presentation.dto.ContactResponse;
import com.unlam.verabackend.presentation.dto.GenerateInvitationResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class ManageContactsUseCaseImpl implements ManageContactsUseCase {

    private final TrustContactRepository trustContactRepository;
    private final TrustInvitationRepository trustInvitationRepository;
    private final UserRepository userRepository;
    private final SseService sseService;

    @Value("${app.frontend.url:http://localhost:5173}")
    private String frontendUrl;

    public ManageContactsUseCaseImpl(TrustContactRepository trustContactRepository, TrustInvitationRepository trustInvitationRepository, UserRepository userRepository, SseService sseService) {
        this.trustContactRepository = trustContactRepository;
        this.trustInvitationRepository = trustInvitationRepository;
        this.userRepository = userRepository;
        this.sseService = sseService;
    }


    @Override
    @Transactional(readOnly = true)
    public List<ContactResponse> getContactsByProtectedPerson(Long protectedPersonId, String caregiverEmail) {
        verifyCaregiverAccess(protectedPersonId, caregiverEmail);

        List<ContactResponse> result = new ArrayList<>();

        trustContactRepository.findByProtectedUserId(protectedPersonId)
                .stream()
                .map(ContactResponse::fromActive)
                .forEach(result::add);

        trustInvitationRepository.findByProtectedPersonIdAndStatus(protectedPersonId, InvitationStatus.PENDING)
                .stream()
                .filter(inv -> inv.getExpiresAt() == null || inv.getExpiresAt().isAfter(LocalDateTime.now()))
                .map(ContactResponse::fromPending)
                .forEach(result::add);

        return result;
    }

    @Override
    @Transactional
    public ContactResponse addContact(Long protectedPersonId, String caregiverEmail, AddContactRequest request) {
        verifyCaregiverAccess(protectedPersonId, caregiverEmail);

        User contactUser = userRepository.findByEmail(request.contactEmail())
                .orElseThrow(() -> new IllegalArgumentException("No existe ningún usuario registrado con el email: " + request.contactEmail()));

        User protectedUser = userRepository.findById(protectedPersonId)
                .orElseThrow(() -> new IllegalArgumentException("Persona protegida no encontrada"));

        if (trustContactRepository.existsByCarerIdAndProtectedUser_Id(contactUser.getId(), protectedPersonId)) {
            throw new IllegalArgumentException("Este usuario ya es contacto de confianza del protegido");
        }

        TrustContact contact = TrustContact.builder()
                .carer(contactUser)
                .protectedUser(protectedUser)
                .relationship(request.relationship())
                .sensitivityLevel(SensitivityLevel.MEDIO)
                .notifyHighRisk(request.emergencyContact())
                .receiveAlertSummaries(false)
                .build();

        return ContactResponse.fromActive(trustContactRepository.save(contact));
    }

    @Override
    @Transactional
    public GenerateInvitationResponse inviteContact(Long protectedPersonId, String caregiverEmail, AddContactRequest request) {
        verifyCaregiverAccess(protectedPersonId, caregiverEmail);

        User caregiver = userRepository.findByEmail(caregiverEmail)
                .orElseThrow(() -> new IllegalArgumentException("Cuidador no encontrado"));

        User protectedUser = userRepository.findById(protectedPersonId)
                .orElseThrow(() -> new IllegalArgumentException("Persona protegida no encontrada"));

        String token = UUID.randomUUID().toString();

        TrustInvitation invitation = TrustInvitation.builder()
                .carer(caregiver)
                .protectedPerson(protectedUser)
                .token(token)
                .fullName(request.fullName())
                .contactNumber(request.contactPhone())
                .email(request.contactEmail())
                .relationship(request.relationship())
                .sensitivityLevel(SensitivityLevel.MEDIO)
                .notifyHighRisk(request.emergencyContact())
                .receiveAlertSummaries(false)
                .expiresAt(LocalDateTime.now().plusDays(7))
                .status(InvitationStatus.PENDING)
                .build();

        TrustInvitation saved = trustInvitationRepository.save(invitation);

        // Si el contacto ya tiene cuenta, le enviamos notificación SSE
        userRepository.findByEmail(request.contactEmail()).ifPresent(invitedUser -> {
            Map<String, Object> payload = Map.of(
                    "id", saved.getId(),
                    "fullName", saved.getFullName(),
                    "caregiverName", caregiver.getFullName(),
                    "relationship", saved.getRelationship()
            );
            sseService.createAndSendNotification(
                    invitedUser,
                    NotificationsType.INVITATION,
                    caregiver.getFullName(),
                    payload
            );
        });

        String fullLink = frontendUrl + "/invite/" + token;
        return new GenerateInvitationResponse(token, fullLink);
    }

    @Override
    @Transactional
    public void updateContact(Long contactId, String caregiverEmail, boolean emergencyContact) {
        TrustContact contact = trustContactRepository.findById(contactId)
                .orElseThrow(() -> new IllegalArgumentException("Contacto no encontrado"));

        verifyCaregiverAccess(contact.getProtectedUser().getId(), caregiverEmail);

        contact.setNotifyHighRisk(emergencyContact);
        trustContactRepository.save(contact);
    }

    @Override
    @Transactional
    public void deleteContact(Long contactId, String caregiverEmail) {
        if (trustContactRepository.existsById(contactId)) {
            TrustContact contact = trustContactRepository.findById(contactId)
                    .orElseThrow(() -> new IllegalArgumentException("Contacto no encontrado"));
            verifyCaregiverAccess(contact.getProtectedUser().getId(), caregiverEmail);
            trustContactRepository.deleteById(contactId);
            return;
        }

        TrustInvitation invitation = trustInvitationRepository.findById(contactId)
                .orElseThrow(() -> new IllegalArgumentException("Contacto o invitación no encontrada"));

        if (invitation.getProtectedPerson() != null) {
            verifyCaregiverAccess(invitation.getProtectedPerson().getId(), caregiverEmail);
        }

        trustInvitationRepository.deleteById(contactId);
    }

    private void verifyCaregiverAccess(Long protectedPersonId, String caregiverEmail) {
        User caregiver = userRepository.findByEmail(caregiverEmail)
                .orElseThrow(() -> new IllegalArgumentException("Cuidador no encontrado"));

        if (!trustContactRepository.existsByCarerIdAndProtectedUser_Id(caregiver.getId(), protectedPersonId)) {
            throw new AccessDeniedException("No tenés acceso a los contactos de esta persona protegida");
        }
    }

}