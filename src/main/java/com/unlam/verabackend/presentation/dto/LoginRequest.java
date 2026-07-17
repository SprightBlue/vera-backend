package com.unlam.verabackend.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "Credenciales necesarias para el inicio de sesión local tradicional.")
public class LoginRequest {

    @NotBlank(message = "El correo electrónico es obligatorio")
    @Email(message = "El correo electrónico no es válido")
    @Schema(description = "Dirección de correo electrónico registrada del usuario", example = "carer@unlam.edu.ar", requiredMode = Schema.RequiredMode.REQUIRED)
    private String email;

    @NotBlank(message = "La contraseña es obligatoria")
    @Schema(description = "Contraseña de la cuenta del usuario", example = "ContraseniaSegura9", requiredMode = Schema.RequiredMode.REQUIRED)
    private String password;
}