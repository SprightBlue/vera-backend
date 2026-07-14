package com.unlam.verabackend.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "Respuesta detallada con la información del perfil del usuario")
public class ProfileResponse {

    @Schema(description = "Identificador único del usuario en el sistema", example = "42")
    private Long id;

    @Schema(description = "Nombre completo del usuario", example = "Juan Carlos Pérez")
    private String fullName;

    @Schema(description = "Dirección de correo electrónico (identificador de cuenta)", example = "juan.perez@ejemplo.com")
    private String email;

    @Schema(description = "Número telefónico de contacto con código de área (opcional)", example = "+541198765432")
    private String phone;

    @Schema(description = "Rol asignado dentro de la plataforma de asistencia", example = "CARER", allowableValues = {"CARER", "PROTECTED"})
    private String role;

    @Schema(description = "Enlace público hacia la foto de perfil almacenada en la nube", example = "https://res.cloudinary.com/verabackend/image/upload/users/juan_perez.png")
    private String imageUrl;
}