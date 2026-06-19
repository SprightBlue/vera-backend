package com.unlam.verabackend.domain.port.in;

import com.unlam.verabackend.presentation.dto.AddContactRequest;
import com.unlam.verabackend.presentation.dto.ContactResponse;
import com.unlam.verabackend.presentation.dto.GenerateInvitationResponse;

import java.util.List;

public interface ManageContactsUseCase {
    List<ContactResponse> getContactsByProtectedPerson(Long protectedPersonId, String caregiverEmail);

    ContactResponse addContact(Long protectedPersonId, String caregiverEmail, AddContactRequest request);

    GenerateInvitationResponse inviteContact(Long protectedPersonId, String caregiverEmail, AddContactRequest request);

    void updateContact(Long contactId, String caregiverEmail, boolean emergencyContact);

    void deleteContact(Long contactId, String caregiverEmail);
}
