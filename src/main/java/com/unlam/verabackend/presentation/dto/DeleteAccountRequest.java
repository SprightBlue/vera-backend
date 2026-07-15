package com.unlam.verabackend.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Estructura requerida para confirmar la baja definitiva de la cuenta del usuario mediante su contraseña.")
public class DeleteAccountRequest {

    @Schema(description = "Contraseña actual del usuario para validar su identidad antes del borrado.", example = "ClaveSegura123!", requiredMode = Schema.RequiredMode.REQUIRED)
    private String password;
}