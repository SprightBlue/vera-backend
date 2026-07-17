package com.unlam.verabackend.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Respuesta unificada tras un inicio de sesión o registro exitoso. Contiene el token JWT de sesión y los detalles de perfil básicos del usuario.")
public class AuthResponse {

    @Schema(description = "ID único de base de datos asignado al usuario", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long id;

    @Schema(description = "Token de acceso JSON Web Token (JWT). En registros locales será null hasta que el email sea verificado.", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String token;

    @Schema(description = "Dirección de correo electrónico principal del usuario", example = "ejemplo@unlam.edu.ar", requiredMode = Schema.RequiredMode.REQUIRED)
    private String email;

    @Schema(description = "Nombre y apellido del usuario registrado", example = "Juan Pérez", requiredMode = Schema.RequiredMode.REQUIRED)
    private String fullName;

    @Schema(description = "Rol del usuario asignado en el sistema", example = "CARER", allowableValues = {"CARER", "PROTECTED"}, requiredMode = Schema.RequiredMode.REQUIRED)
    private String role;

    @Schema(description = "URL de la imagen de perfil del usuario. Puede ser nula si no se ha configurado.", example = "https://vera-bucket.s3.amazonaws.com/profiles/avatar.png", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String image;

    public AuthResponse(Long id, String token, String email, String fullName, String role) {
        this.id = id;
        this.token = token;
        this.email = email;
        this.fullName = fullName;
        this.role = role;
        this.image = null;
    }
}