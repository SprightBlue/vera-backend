package com.unlam.verabackend.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Estructura requerida para actualizar los datos básicos del perfil")
public class UpdateProfileRequest {

    @Schema(description = "Nombre y apellido actualizados del usuario", example = "Juan Carlos Pérez", requiredMode = Schema.RequiredMode.REQUIRED)
    private String fullName;

    @Schema(description = "Nuevo número de teléfono con código de país", example = "+541198765432", requiredMode = Schema.RequiredMode.REQUIRED)
    private String phone;
}