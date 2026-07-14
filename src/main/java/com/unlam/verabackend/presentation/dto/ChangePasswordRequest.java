package com.unlam.verabackend.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Estructura requerida para realizar el cambio seguro de contraseña de acceso")
public class ChangePasswordRequest {

    @Schema(description = "Contraseña vigente del usuario utilizada para validar su identidad", example = "ClaveAnterior123*", requiredMode = Schema.RequiredMode.REQUIRED)
    private String currentPassword;

    @Schema(description = "Nueva contraseña que se desea establecer en el sistema", example = "NuevaSuperClave2026!", requiredMode = Schema.RequiredMode.REQUIRED)
    private String newPassword;

    @Schema(description = "Confirmación exacta de la nueva contraseña propuesta", example = "NuevaSuperClave2026!", requiredMode = Schema.RequiredMode.REQUIRED)
    private String confirmPassword;
}