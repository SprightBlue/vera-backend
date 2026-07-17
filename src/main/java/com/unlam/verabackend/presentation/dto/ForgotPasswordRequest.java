package com.unlam.verabackend.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "Solicitud de recuperación de contraseña para el inicio del proceso de restablecimiento.")
public class ForgotPasswordRequest {

    @NotBlank(message = "El correo electrónico es obligatorio")
    @Email(message = "El correo electrónico no es válido")
    @Schema(description = "Correo electrónico de la cuenta que se desea recuperar", example = "usuario@unlam.edu.ar", requiredMode = Schema.RequiredMode.REQUIRED)
    private String email;
}