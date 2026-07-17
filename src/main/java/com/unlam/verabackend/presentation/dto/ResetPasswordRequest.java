package com.unlam.verabackend.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "Estructura requerida para aplicar el restablecimiento de la contraseña mediante token de recuperación.")
public class ResetPasswordRequest {

    @NotBlank(message = "El token de recuperación es obligatorio")
    @Schema(description = "Token UUID único enviado previamente al correo del usuario", example = "d3b07384-d113-4ec6-a5d7-ec48e02d8b51", requiredMode = Schema.RequiredMode.REQUIRED)
    private String token;

    @NotBlank(message = "La nueva contraseña es obligatoria")
    @Size(min = 6, max = 40, message = "La nueva contraseña debe tener entre 6 y 40 caracteres")
    @Schema(description = "Nueva credencial de acceso para la cuenta del usuario", example = "ClaveNuevaS9!", requiredMode = Schema.RequiredMode.REQUIRED)
    private String newPassword;
}