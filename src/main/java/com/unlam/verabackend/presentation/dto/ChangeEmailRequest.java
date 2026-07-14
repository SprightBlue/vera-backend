package com.unlam.verabackend.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Estructura requerida para cambiar el correo electrónico asociado a la cuenta")
public class ChangeEmailRequest {

    @Schema(description = "Nueva dirección de correo electrónico a registrar, debe ser única en el sistema", example = "nuevo.correo@ejemplo.com", requiredMode = Schema.RequiredMode.REQUIRED)
    private String newEmail;

    @Schema(description = "Contraseña actual del usuario para autorizar la operación crítica", example = "ClaveSegura123*", requiredMode = Schema.RequiredMode.REQUIRED)
    private String password;
}