package com.unlam.verabackend.domain.ports.in;

import com.unlam.verabackend.presentation.dto.AddContactRequest;
import com.unlam.verabackend.presentation.dto.ContactResponse;

import java.util.List;

public interface ManageContactsUseCase {
    List<ContactResponse> getContactsByProtectedPerson(Long protectedPersonId, String caregiverEmail);

    ContactResponse addContact(Long protectedPersonId, String caregiverEmail, AddContactRequest request);

    String generateInviteLink(Long protectedPersonId, String caregiverEmail, String contactEmail, String relationship);
}
