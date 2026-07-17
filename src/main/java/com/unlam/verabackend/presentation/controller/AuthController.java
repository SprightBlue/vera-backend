package com.unlam.verabackend.presentation.controller;

import com.unlam.verabackend.domain.port.in.UserUseCase;
import com.unlam.verabackend.presentation.dto.AuthResponse;
import com.unlam.verabackend.presentation.dto.LoginRequest;
import com.unlam.verabackend.presentation.dto.RegisterRequest;
import com.unlam.verabackend.presentation.dto.ResetPasswordRequest;
import com.unlam.verabackend.presentation.dto.ForgotPasswordRequest;
import com.unlam.verabackend.presentation.dto.GoogleLoginRequest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticación", description = "Endpoints públicos para el flujo de registro, inicio de sesión, verificación de correo y recuperación de credenciales")
public class AuthController {

    private final UserUseCase userService;

    @PostMapping("/register")
    @Operation(
            summary = "Registrar un nuevo usuario en el sistema",
            description = "Crea un registro de usuario de forma inactiva y dispara el proceso de verificación enviando un token al correo electrónico indicado.",
            responses = {
                    @ApiResponse(
                            responseCode = "201",
                            description = "Usuario registrado exitosamente en estado inactivo",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = AuthResponse.class))
                    ),
                    @ApiResponse(responseCode = "400", description = "Datos provistos inválidos o no se aceptaron los términos y condiciones", content = @Content),
                    @ApiResponse(responseCode = "409", description = "El correo electrónico provisto ya se encuentra registrado en el sistema", content = @Content)
            }
    )
    public ResponseEntity<AuthResponse> register(
            @Valid @RequestBody RegisterRequest request) throws Exception {
        log.info("REST Request: POST - Solicitando registro de nuevo usuario con email [{}]", request.getEmail());
        AuthResponse response = userService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    @Operation(
            summary = "Iniciar sesión mediante credenciales locales",
            description = "Valida las credenciales de acceso del usuario y retorna un token JWT válido junto con los datos esenciales de su perfil.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Autenticación exitosa",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = AuthResponse.class))
                    ),
                    @ApiResponse(responseCode = "400", description = "Formato de solicitud incorrecto o cuenta no verificada aún", content = @Content),
                    @ApiResponse(responseCode = "401", description = "Credenciales de acceso incorrectas", content = @Content),
                    @ApiResponse(responseCode = "404", description = "El usuario con el email provisto no existe", content = @Content)
            }
    )
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody LoginRequest request) {
        log.info("REST Request: POST - Solicitando inicio de sesión local para el email [{}]", request.getEmail());
        AuthResponse response = userService.login(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/google")
    @Operation(
            summary = "Iniciar sesión o registrarse mediante Google Identity",
            description = "Valida el ID Token provisto por el SDK de Google Client. Si el usuario no existe en la plataforma, lo registra de forma activa con el rol provisto.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Autenticación mediante Google exitosa",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = AuthResponse.class))
                    ),
                    @ApiResponse(responseCode = "400", description = "Token de Google inválido, corrupto o expirado", content = @Content),
                    @ApiResponse(responseCode = "403", description = "Violación de seguridad al procesar las credenciales de terceros", content = @Content)
            }
    )
    public ResponseEntity<AuthResponse> googleLogin(
            @Valid @RequestBody GoogleLoginRequest request) {
        log.info("REST Request: POST - Solicitando autenticación federada vía Google");
        AuthResponse response = userService.googleLogin(
                request.getCredential(),
                request.getRole()
        );
        return ResponseEntity.ok(response);
    }

    @PostMapping("/forgot-password")
    @Operation(
            summary = "Solicitar restablecimiento de contraseña",
            description = "Genera un token temporal de recuperación y envía un enlace por correo electrónico si el usuario existe en el sistema.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Proceso de recuperación iniciado y correo enviado correctamente", content = @Content),
                    @ApiResponse(responseCode = "404", description = "No existe un usuario registrado bajo el correo electrónico provisto", content = @Content)
            }
    )
    public ResponseEntity<Void> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request) throws Exception {
        log.info("REST Request: POST - Solicitando recuperación de clave para el email [{}]", request.getEmail());
        userService.forgotPassword(request.getEmail());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/reset-password")
    @Operation(
            summary = "Restablecer contraseña mediante token de recuperación",
            description = "Valida el token de recuperación provisto y actualiza la contraseña del usuario asociado si no ha expirado y no se ha usado previamente.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Contraseña restablecida de forma exitosa", content = @Content),
                    @ApiResponse(responseCode = "400", description = "Token expirado, previamente utilizado o inexistente", content = @Content),
                    @ApiResponse(responseCode = "404", description = "Token no encontrado en los registros", content = @Content)
            }
    )
    public ResponseEntity<Void> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request) {
        log.info("REST Request: POST - Solicitando cambio de contraseña usando token de recuperación");
        userService.resetPassword(
                request.getToken(),
                request.getNewPassword()
        );
        return ResponseEntity.ok().build();
    }

    @GetMapping("/verify")
    @Operation(
            summary = "Verificar la dirección de correo electrónico del usuario",
            description = "Activa de forma absoluta la cuenta del usuario utilizando el token enviado durante su registro inicial si este no ha caducado.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Cuenta verificada con éxito",
                            content = @Content(mediaType = "text/plain")
                    ),
                    @ApiResponse(responseCode = "400", description = "El token de verificación ha expirado", content = @Content),
                    @ApiResponse(responseCode = "404", description = "Token de verificación no encontrado o inválido", content = @Content)
            }
    )
    public ResponseEntity<String> verifyEmail(
            @Parameter(description = "Token único de verificación de email enviado al correo", required = true)
            @RequestParam("token") String token) {
        log.info("REST Request: GET - Iniciando validación de email por token");
        userService.verifyEmail(token);
        return ResponseEntity.ok("¡Cuenta verificada con éxito! Ya podés iniciar sesión en VERA.");
    }
}