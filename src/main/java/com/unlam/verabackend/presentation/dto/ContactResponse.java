package com.unlam.verabackend.presentation.dto;

import com.unlam.verabackend.infrastructure.entity.TrustContact;
import com.unlam.verabackend.infrastructure.entity.TrustInvitation;

public record ContactResponse(
        Long id,
        String fullName,
        String email,
        String phone,
        String relationship,
        boolean emergencyContact,
        String status
)  {
    public static ContactResponse fromActive(TrustContact contact) {
        return new ContactResponse(
                contact.getId(),
                contact.getCarer().getFullName(),
                contact.getCarer().getEmail(),
                null,
                contact.getRelationship(),
                contact.isNotifyHighRisk(),
                "ACTIVE"
        );
    }
    public static ContactResponse fromPending(TrustInvitation invitation) {
        return new ContactResponse(
                invitation.getId(),
                invitation.getFullName(),
                invitation.getEmail(),
                invitation.getContactNumber(),
                invitation.getRelationship(),
                invitation.isNotifyHighRisk(),
                "PENDING"
        );
    }
}