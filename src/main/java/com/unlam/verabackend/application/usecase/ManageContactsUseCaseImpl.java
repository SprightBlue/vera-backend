package com.unlam.verabackend.application.usecase;

import com.unlam.verabackend.domain.model.*;
import com.unlam.verabackend.domain.ports.in.ManageContactsUseCase;
import com.unlam.verabackend.domain.repository.UserRepository;
import com.unlam.verabackend.entity.SensitivityLevel;
import com.unlam.verabackend.entity.TrustContact;
import com.unlam.verabackend.infrastructure.entity.User;
import com.unlam.verabackend.infrastructure.repository.TrustContactRepository;
import com.unlam.verabackend.infrastructure.repository.TrustInvitationRepository;
import com.unlam.verabackend.presentation.dto.AddContactRequest;
import com.unlam.verabackend.presentation.dto.ContactResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class ManageContactsUseCaseImpl implements ManageContactsUseCase {

    private final TrustContactRepository trustContactRepository;
    private final TrustInvitationRepository trustInvitationRepository;
    private final UserRepository userRepository;

    @Value("${app.frontend.url:http://localhost:5173}")
    private String frontendUrl;

    public ManageContactsUseCaseImpl(TrustContactRepository trustContactRepository, TrustInvitationRepository trustInvitationRepository, UserRepository userRepository) {
        this.trustContactRepository = trustContactRepository;
        this.trustInvitationRepository = trustInvitationRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<ContactResponse> getContactsByProtectedPerson(Long protectedPersonId, String caregiverEmail) {
        verifyCaregiverAccess(protectedPersonId, caregiverEmail);
        return trustContactRepository.findByProtectedUserId(protectedPersonId)
                .stream()
                .map(ContactResponse::from)
                .toList();
    }

    @Transactional
    public ContactResponse addContact(Long protectedPersonId, String caregiverEmail, AddContactRequest request) {
        verifyCaregiverAccess(protectedPersonId, caregiverEmail);
        User contactUser = userRepository.findByEmail(request.contactEmail()).orElseThrow(() -> new IllegalArgumentException("No existe ningún usuario con email: " + request.contactEmail()));
        User protectedUser = userRepository.findById(protectedPersonId).orElseThrow(() -> new IllegalArgumentException("Persona protegida no encontrada"));

        if (trustContactRepository.existsByCarerIdAndProtectedUserId(contactUser.getId(), protectedPersonId)) {
            throw new IllegalArgumentException("Este usuario ya es contacto de emergencia del protegido");
        }

        TrustContact contact = TrustContact.builder()
                .carer(contactUser)
                .protectedUser(protectedUser)
                .relationship(request.relationship())
                .sensitivityLevel(SensitivityLevel.MEDIO)
                .notifyHighRisk(true)
                .receiveAlertSummaries(false)
                .build();
        return ContactResponse.from(trustContactRepository.save(contact));
    }

    @Transactional
    public String generateInviteLink(Long protectedPersonId, String caregiverEmail, String contactEmail, String relationship) {
        verifyCaregiverAccess(protectedPersonId, caregiverEmail);
        User caregiver = userRepository.findByEmail(caregiverEmail).orElseThrow(() -> new IllegalArgumentException("Cuidador no encontrado"));

        String token = UUID.randomUUID().toString();
        var invitation = com.unlam.verabackend.entity.TrustInvitation.builder()
                .carer(caregiver)
                .token(token)
                .fullName(contactEmail)
                .email(contactEmail)
                .relationship(relationship)
                .sensitivityLevel(com.unlam.verabackend.entity.SensitivityLevel.MEDIO)
                .notifyHighRisk(true)
                .receiveAlertSummaries(false)
                .build();
        trustInvitationRepository.save(invitation);
        return frontendUrl + "/invite/" + token;
    }

    private void verifyCaregiverAccess(Long protectedPersonId, String caregiverEmail) {
        User caregiver = userRepository.findByEmail(caregiverEmail).orElseThrow(() -> new IllegalArgumentException("Cuidador no encontrado"));
        if (!trustContactRepository.existsByCarerIdAndProtectedUserId(caregiver.getId(), protectedPersonId)) {
            throw new AccessDeniedException("No tenés acceso a los contactos de esta persona protegida");
        }
    }
}
