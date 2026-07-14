package com.unlam.verabackend.presentation.controller;

import com.unlam.verabackend.domain.port.in.UserSettingsUseCase;
import com.unlam.verabackend.infrastructure.entity.User;
import com.unlam.verabackend.presentation.dto.ChangeEmailRequest;
import com.unlam.verabackend.presentation.dto.ChangePasswordRequest;
import com.unlam.verabackend.presentation.dto.ProfileResponse;
import com.unlam.verabackend.presentation.dto.UpdateProfileRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@Slf4j
@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
@Tag(name = "Usuario", description = "Endpoints para la gestión de perfil, actualización de credenciales de seguridad y remoción de cuentas")
public class UserSettingsController {

    private final UserSettingsUseCase userSettingsUseCase;

    @GetMapping("/profile")
    @Operation(
            summary = "Obtener el perfil del usuario autenticado",
            description = "Recupera la información básica del perfil del operador actualmente logueado en base a su token de sesión.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Datos del perfil obtenidos exitosamente",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProfileResponse.class))
                    ),
                    @ApiResponse(responseCode = "401", description = "No autorizado - Token JWT ausente o inválido", content = @Content),
                    @ApiResponse(responseCode = "404", description = "No se encontró el registro del usuario en la base de datos", content = @Content)
            }
    )
    public ResponseEntity<ProfileResponse> getProfile(
            @AuthenticationPrincipal @Parameter(hidden = true) User user
    ) {
        log.info("REST Request: GET - Solicitando lectura de datos de perfil para [{}]", user.getEmail());
        ProfileResponse profile = userSettingsUseCase.getProfile(user.getEmail());
        return ResponseEntity.ok(profile);
    }

    @PutMapping("/profile")
    @Operation(
            summary = "Actualizar información personal del usuario",
            description = "Modifica datos modificables del perfil del usuario autenticado como su nombre completo y teléfono.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Perfil actualizado exitosamente",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProfileResponse.class))
                    ),
                    @ApiResponse(responseCode = "400", description = "Datos provistos inválidos o formato incorrecto", content = @Content),
                    @ApiResponse(responseCode = "401", description = "No autorizado - Token JWT ausente o inválido", content = @Content)
            }
    )
    public ResponseEntity<ProfileResponse> updateProfile(
            @AuthenticationPrincipal @Parameter(hidden = true) User user,
            @Valid @RequestBody UpdateProfileRequest request
    ) {
        log.info("REST Request: PUT - Solicitando actualización de campos de perfil para [{}]", user.getEmail());
        ProfileResponse updatedProfile = userSettingsUseCase.updateProfile(user.getEmail(), request);
        return ResponseEntity.ok(updatedProfile);
    }

    @PutMapping("/password")
    @Operation(
            summary = "Cambiar contraseña de la cuenta",
            description = "Modifica la credencial de acceso del usuario autenticado previa validación de su contraseña actual.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Contraseña modificada correctamente", content = @Content),
                    @ApiResponse(responseCode = "400", description = "Error de validación (la clave actual es errónea, las nuevas claves no coinciden, o es igual a la actual)", content = @Content),
                    @ApiResponse(responseCode = "412", description = "La solicitud no cumple con los requisitos de seguridad obligatorios", content = @Content)
            }
    )
    public ResponseEntity<Void> changePassword(
            @AuthenticationPrincipal @Parameter(hidden = true) User user,
            @Valid @RequestBody ChangePasswordRequest request
    ) {
        log.info("REST Request: PUT - Solicitando cambio de credencial de acceso para [{}]", user.getEmail());
        userSettingsUseCase.changePassword(user.getEmail(), request);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/email")
    @Operation(
            summary = "Cambiar correo electrónico de la cuenta",
            description = "Modifica el correo electrónico principal (identificador único) de la cuenta si el nuevo email no se encuentra en uso por otro usuario.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Correo electrónico actualizado correctamente", content = @Content),
                    @ApiResponse(responseCode = "400", description = "La contraseña de confirmación es incorrecta o el nuevo correo electrónico ya se encuentra registrado", content = @Content),
                    @ApiResponse(responseCode = "401", description = "No autorizado - Token JWT ausente o inválido", content = @Content)
            }
    )
    public ResponseEntity<Void> changeEmail(
            @AuthenticationPrincipal @Parameter(hidden = true) User user,
            @Valid @RequestBody ChangeEmailRequest request
    ) {
        log.info("REST Request: PUT - Solicitando actualización de email para [{}]", user.getEmail());
        userSettingsUseCase.changeEmail(user.getEmail(), request);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping
    @Operation(
            summary = "Dar de baja definitivamente la cuenta",
            description = "Inicia el borrado lógico/físico permanente del usuario autenticado del sistema de manera segura. Valida que no tenga vínculos activos.",
            responses = {
                    @ApiResponse(responseCode = "204", description = "Cuenta del usuario removida con éxito de los registros", content = @Content),
                    @ApiResponse(responseCode = "400", description = "Imposible remover cuenta (el usuario posee dependencias activas como personas protegidas o cuidadores)", content = @Content),
                    @ApiResponse(responseCode = "401", description = "No autorizado - Token JWT ausente o inválido", content = @Content)
            }
    )
    public ResponseEntity<Void> deleteAccount(
            @AuthenticationPrincipal @Parameter(hidden = true) User user
    ) throws IOException {
        log.info("REST Request: DELETE - Iniciando solicitud de remoción absoluta de cuenta para el usuario [{}]", user.getEmail());
        userSettingsUseCase.deleteAccount(user.getEmail());
        return ResponseEntity.noContent().build();
    }
}