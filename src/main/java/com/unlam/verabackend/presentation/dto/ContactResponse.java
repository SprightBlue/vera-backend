package com.unlam.verabackend.presentation.dto;

import com.unlam.verabackend.entity.TrustContact;

public record ContactResponse(
        Long id,
        String fullName,
        String email,
        String relationship,
        String status
) {
    public static ContactResponse from(TrustContact contact) {
        return new ContactResponse(
                contact.getId(),
                contact.getCarer().getFullName(),
                contact.getCarer().getEmail(),
                contact.getRelationship(),
                "ACTIVE"
        );
    }
}