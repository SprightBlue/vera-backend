package com.unlam.verabackend.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "Estructura requerida para el registro de un nuevo usuario local en el sistema.")
public class RegisterRequest {

    @NotBlank(message = "El nombre de usuario es obligatorio")
    @Size(min = 3, max = 20, message = "El nombre de usuario debe tener entre 3 y 20 caracteres")
    @Schema(description = "Nombre completo del usuario que se registrará en la plataforma", example = "María Gómez", requiredMode = Schema.RequiredMode.REQUIRED)
    private String fullName;

    @NotBlank(message = "El correo electrónico es obligatorio")
    @Email(message = "El correo electrónico no es válido")
    @Schema(description = "Correo electrónico que servirá como identificador único de cuenta", example = "maria.gomez@unlam.edu.ar", requiredMode = Schema.RequiredMode.REQUIRED)
    private String email;

    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 6, max = 40, message = "La contraseña debe tener entre 6 y 40 caracteres")
    @Schema(description = "Contraseña de acceso para el login local", example = "Segura123!", requiredMode = Schema.RequiredMode.REQUIRED)
    private String password;

    @NotBlank(message = "El rol es obligatorio")
    @Pattern(regexp = "^(CARER|PROTECTED)$", message = "Rol inválido. Debe ser CARER o PROTECTED")
    @Schema(description = "Rol operativo inicial del usuario dentro de la plataforma VERA", example = "CARER", allowableValues = {"CARER", "PROTECTED"}, requiredMode = Schema.RequiredMode.REQUIRED)
    private String role;

    @NotNull(message = "Debe especificar explícitamente la aceptación de los términos y condiciones")
    @Schema(description = "Indicador de aceptación de los términos de servicio y políticas de privacidad de VERA. Debe ser true para permitir el registro.", example = "true", requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean acceptedTerms;
}