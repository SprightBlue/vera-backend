package com.unlam.verabackend.presentation.controller;

import com.unlam.verabackend.domain.ports.in.ManageContactsUseCase;
import com.unlam.verabackend.presentation.dto.AddContactRequest;
import com.unlam.verabackend.presentation.dto.InviteLinkResponse;
import com.unlam.verabackend.presentation.dto.ContactResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/contacts")
public class ContactController {

    private final ManageContactsUseCase manageContactsUseCase;

    public ContactController(ManageContactsUseCase manageContactsUseCase) {
        this.manageContactsUseCase = manageContactsUseCase;
    }

    @GetMapping("/protected-person/{protectedPersonId}")
    public ResponseEntity<List<ContactResponse>> getContacts(@PathVariable Long protectedPersonId, Authentication authentication) {
        return ResponseEntity.ok(manageContactsUseCase.getContactsByProtectedPerson(protectedPersonId, authentication.getName()));
    }

    @PostMapping("/protected-person/{protectedPersonId}")
    public ResponseEntity<ContactResponse> addContact(@PathVariable Long protectedPersonId, @RequestBody AddContactRequest request, Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED).body(manageContactsUseCase.addContact(protectedPersonId, authentication.getName(), request));
    }

    @PostMapping("/protected-person/{protectedPersonId}/invite")
    public ResponseEntity<InviteLinkResponse> generateInviteLink(@PathVariable Long protectedPersonId, @RequestBody AddContactRequest request, Authentication authentication) {
        String link = manageContactsUseCase.generateInviteLink(protectedPersonId, authentication.getName(), request.contactEmail(), request.relationship());
        return ResponseEntity.ok(new InviteLinkResponse(link));
    }
}
