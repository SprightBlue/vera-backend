package com.unlam.verabackend.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
@Schema(description = "Estructura de autenticación federada mediante el proveedor Google Identity.")
public class GoogleLoginRequest {

    @NotBlank(message = "La credencial provista por Google es obligatoria")
    @Schema(description = "ID Token JWT generado por el SDK cliente de Google tras la confirmación de identidad", example = "eyJhbGciOiJSUzI1NiIsImtpZCI6...", requiredMode = Schema.RequiredMode.REQUIRED)
    private String credential;

    @Pattern(regexp = "^(CARER|PROTECTED)$", message = "Rol inválido. Debe ser CARER o PROTECTED")
    @Schema(description = "Rol sugerido para el usuario en caso de que sea su primer ingreso en VERA y requiera un registro automático", example = "PROTECTED", allowableValues = {"CARER", "PROTECTED"}, requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String role;
}