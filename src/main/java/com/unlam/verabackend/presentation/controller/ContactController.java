package com.unlam.verabackend.presentation.controller;

import com.unlam.verabackend.domain.port.in.ManageContactsUseCase;
import com.unlam.verabackend.presentation.dto.AddContactRequest;
import com.unlam.verabackend.presentation.dto.ContactResponse;
import com.unlam.verabackend.presentation.dto.GenerateInvitationResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/contacts")
@RequiredArgsConstructor
public class ContactController {

    private final ManageContactsUseCase manageContactsUseCase;

    @GetMapping("/protected-person/{protectedPersonId}")
    public ResponseEntity<List<ContactResponse>> getContacts(@PathVariable Long protectedPersonId, Authentication authentication) {return ResponseEntity.ok(manageContactsUseCase.getContactsByProtectedPerson(protectedPersonId, authentication.getName())
        );
    }

    @PostMapping("/protected-person/{protectedPersonId}")
    public ResponseEntity<ContactResponse> addContact(
            @PathVariable Long protectedPersonId,
            @Valid @RequestBody AddContactRequest request,
            Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
                manageContactsUseCase.addContact(protectedPersonId, authentication.getName(), request)
        );
    }

    @PostMapping("/protected-person/{protectedPersonId}/invite")
    public ResponseEntity<GenerateInvitationResponse> inviteContact(@PathVariable Long protectedPersonId, @Valid @RequestBody AddContactRequest request, Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED).body(manageContactsUseCase.inviteContact(protectedPersonId, authentication.getName(), request));
    }

    @PatchMapping("/{contactId}")
    public ResponseEntity<Void> updateContact(@PathVariable Long contactId, @RequestBody Map<String, Object> payload, Authentication authentication) {
        boolean emergencyContact = Boolean.TRUE.equals(payload.get("emergencyContact"));
        manageContactsUseCase.updateContact(contactId, authentication.getName(), emergencyContact);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{contactId}")
    public ResponseEntity<Void> deleteContact(@PathVariable Long contactId, Authentication authentication) {
        manageContactsUseCase.deleteContact(contactId, authentication.getName());
        return ResponseEntity.noContent().build();
    }
}