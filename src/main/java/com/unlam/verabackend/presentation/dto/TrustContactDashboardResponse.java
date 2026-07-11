package com.unlam.verabackend.presentation.dto;

import com.unlam.verabackend.domain.model.Role;
import com.unlam.verabackend.infrastructure.entity.TrustContact;
import com.unlam.verabackend.infrastructure.entity.User;
import com.unlam.verabackend.presentation.utils.DateFormatter;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "DTO que expone la información resumida del último contacto de confianza y el usuario opuesto")
public class TrustContactDashboardResponse {

    @Schema(description = "ID del contacto de confianza", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long id;

    @Schema(description = "Fecha y hora de creación del vínculo", example = "2026-03-30T10:15:30", requiredMode = Schema.RequiredMode.REQUIRED)
    private String createdAt;

    @Schema(description = "ID del usuario contrario (protegido o cuidador)", example = "5", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long oppositeUserId;

    @Schema(description = "Nombre completo del usuario contrario", example = "Juan Pérez", requiredMode = Schema.RequiredMode.REQUIRED)
    private String oppositeUserFullName;

    @Schema(description = "Rol del usuario contrario", example = "PROTECTED", allowableValues = {"CARER", "PROTECTED"}, requiredMode = Schema.RequiredMode.REQUIRED)
    private Role oppositeUserRole;

    @Schema(description = "Email del usuario contrario", example = "juan.perez@example.com", requiredMode = Schema.RequiredMode.REQUIRED)
    private String oppositeUserEmail;

    @Schema(description = "URL o path de la imagen de perfil del usuario contrario", example = "https://bucket.s3.com/profiles/user5.jpg", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String oppositeUserImage;

    public static TrustContactDashboardResponse fromEntity(TrustContact contact, Role currentRole) {
        if (contact == null) return null;

        User oppositeUser = (currentRole == Role.CARER) ? contact.getProtectedUser() : contact.getCarer();

        return TrustContactDashboardResponse.builder()
                .id(contact.getId())
                .createdAt(DateFormatter.formatRelativeDate(contact.getCreatedAt()))
                .oppositeUserId(oppositeUser.getId())
                .oppositeUserFullName(oppositeUser.getFullName())
                .oppositeUserRole(oppositeUser.getRole())
                .oppositeUserEmail(oppositeUser.getEmail())
                .oppositeUserImage(oppositeUser.getImage())
                .build();
    }
}