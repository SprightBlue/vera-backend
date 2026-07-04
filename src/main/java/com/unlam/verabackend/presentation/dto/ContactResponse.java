package com.unlam.verabackend.presentation.dto;

import com.unlam.verabackend.infrastructure.entity.TrustContact;
import com.unlam.verabackend.infrastructure.entity.TrustInvitation;

public record ContactResponse(
        Long id,
        String fullName,
        String email,
        String phone,
        String relationship,
        String sensitivityLevel,
        boolean notifyHighRisk,
        boolean receiveAlertSummaries,
        String status
) {
    public static ContactResponse fromActive(TrustContact contact) {
        return new ContactResponse(
                contact.getId(),
                contact.getCarer().getFullName(),
                contact.getCarer().getEmail(),
                null,
                contact.getRelationship(),
                contact.getSensitivityLevel() != null ? contact.getSensitivityLevel().name() : "MEDIO",
                contact.isNotifyHighRisk(),
                contact.isReceiveAlertSummaries(),
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
                invitation.getSensitivityLevel() != null ? invitation.getSensitivityLevel().name() : "MEDIO",
                invitation.isNotifyHighRisk(),
                invitation.isReceiveAlertSummaries(),
                "PENDING"
        );
    }
}