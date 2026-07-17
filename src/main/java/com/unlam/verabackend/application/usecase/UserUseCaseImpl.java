package com.unlam.verabackend.application.usecase;

import com.unlam.verabackend.application.service.JwtService;
import com.unlam.verabackend.presentation.dto.AuthResponse;
import com.unlam.verabackend.presentation.dto.LoginRequest;
import com.unlam.verabackend.presentation.dto.RegisterRequest;
import com.unlam.verabackend.domain.model.Role;
import com.unlam.verabackend.domain.exception.ResourceNotFoundException;
import com.unlam.verabackend.infrastructure.entity.User;
import com.unlam.verabackend.infrastructure.entity.VerificationToken;
import com.unlam.verabackend.infrastructure.repository.UserRepository;
import com.unlam.verabackend.infrastructure.repository.VerificationTokenRepository;
import com.unlam.verabackend.domain.port.in.UserUseCase;
import com.unlam.verabackend.domain.port.out.EmailProvider;
import com.unlam.verabackend.infrastructure.entity.PasswordResetToken;
import com.unlam.verabackend.infrastructure.repository.PasswordResetTokenRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserUseCaseImpl implements UserUseCase {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final PasswordResetTokenRepository tokenRepository;
    private final VerificationTokenRepository verificationTokenRepository;
    private final EmailProvider emailProvider;

    @Value("${google.client-id}")
    private String googleClientId;

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        log.info("Iniciando proceso de registro para el email: {}", request.getEmail());

        if (request.getAcceptedTerms() == null || !request.getAcceptedTerms()) {
            log.warn("Registro rechazado para {}: no se aceptaron los términos y condiciones", request.getEmail());
            throw new IllegalArgumentException("Debe aceptar los términos y condiciones");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            log.warn("Registro fallido: el email {} ya está en uso", request.getEmail());
            throw new IllegalStateException("El correo electrónico ya está registrado");
        }

        User user = new User();
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.valueOf(request.getRole()));
        user.setEnabled(false);

        User savedUser = userRepository.saveAndFlush(user);
        log.debug("Entidad de usuario persistida inmediatamente con ID {} de forma inactiva", savedUser.getId());

        String tokenDeEmail = UUID.randomUUID().toString();
        VerificationToken verificationToken = new VerificationToken(tokenDeEmail, savedUser);
        verificationTokenRepository.save(verificationToken);

        emailProvider.sendVerificationEmail(savedUser.getEmail(), tokenDeEmail);
        log.info("Email de verificación enviado exitosamente a: {}", savedUser.getEmail());

        return buildAuthResponse(savedUser, null);
    }

    @Override
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        log.info("Intento de inicio de sesión para el email: {}", request.getEmail());

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()));

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> {
                    log.error("Fallo de autenticación: el usuario {} no existe tras la validación", request.getEmail());
                    return new ResourceNotFoundException("Usuario no encontrado");
                });

        if (!user.isEnabled()) {
            log.warn("Intento de login fallido para el usuario deshabilitado: {}", request.getEmail());
            throw new IllegalStateException("La cuenta de usuario no está verificada.");
        }

        String token = jwtService.generateToken(user);
        log.info("Inicio de sesión exitoso para el email: {}", request.getEmail());

        return buildAuthResponse(user, token);
    }

    @Override
    @Transactional
    public AuthResponse googleLogin(String credential, String selectedRole) {
        log.info("Iniciando autenticación vía Google de terceros");
        GoogleIdToken.Payload payload = verifyGoogleToken(credential);

        User user = findOrCreateGoogleUser(payload, selectedRole);
        String token = jwtService.generateToken(user);

        log.info("Autenticación con Google exitosa para el email: {}", user.getEmail());
        return buildAuthResponse(user, token);
    }

    private GoogleIdToken.Payload verifyGoogleToken(String credential) {
        try {
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                    GoogleNetHttpTransport.newTrustedTransport(),
                    GsonFactory.getDefaultInstance())
                    .setAudience(Collections.singletonList(googleClientId))
                    .build();

            GoogleIdToken idToken = verifier.verify(credential);
            if (idToken == null) {
                log.warn("El token provisto por Google es inválido o se encuentra expirado");
                throw new IllegalArgumentException("Token de Google inválido o expirado");
            }
            return idToken.getPayload();
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error crítico durante la comunicación con las APIs de Google", e);
            throw new SecurityException("Error al verificar credencial de Google", e);
        }
    }

    private User findOrCreateGoogleUser(GoogleIdToken.Payload payload, String selectedRole) {
        String email = payload.getEmail();
        return userRepository.findByEmail(email)
                .orElseGet(() -> {
                    log.info("Usuario de Google no registrado previamente. Creando nueva cuenta para: {}", email);
                    return createGoogleUser(email, (String) payload.get("name"), selectedRole);
                });
    }

    private User createGoogleUser(String email, String fullName, String selectedRole) {
        String role = (selectedRole != null && !selectedRole.isBlank()) ? selectedRole : "CARER";
        User user = User.builder()
                .email(email)
                .fullName(fullName)
                .password(passwordEncoder.encode(UUID.randomUUID().toString()))
                .role(Role.valueOf(role))
                .enabled(true)
                .build();
        return userRepository.save(user);
    }

    @Override
    @Transactional
    public void forgotPassword(String email) {
        log.info("Solicitud de recuperación de contraseña para el email: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.warn("Recuperación de contraseña fallida: {} no se encuentra registrado", email);
                    return new ResourceNotFoundException("Usuario no encontrado");
                });

        String token = UUID.randomUUID().toString();
        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setToken(token);
        resetToken.setUser(user);
        resetToken.setExpiresAt(LocalDateTime.now().plusHours(1));

        tokenRepository.save(resetToken);
        emailProvider.sendPasswordResetEmail(user.getEmail(), token);

        log.info("Token de recuperación generado y enviado correctamente al email: {}", email);
    }

    @Override
    @Transactional
    public void resetPassword(String token, String newPassword) {
        log.info("Procesando reestablecimiento de contraseña usando token de recuperación");

        PasswordResetToken resetToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> {
                    log.warn("Intento de recuperación con un token inexistente");
                    return new ResourceNotFoundException("Token inválido");
                });

        if (resetToken.isUsed()) {
            log.warn("El token ya fue utilizado previamente para el usuario: {}", resetToken.getUser().getEmail());
            throw new IllegalStateException("Token ya utilizado");
        }

        if (resetToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            log.warn("El token de recuperación ha caducado para el usuario: {}", resetToken.getUser().getEmail());
            throw new IllegalStateException("Token expirado");
        }

        User user = resetToken.getUser();
        updatePassword(user, newPassword);

        resetToken.setUsed(true);
        tokenRepository.save(resetToken);

        log.info("La contraseña se ha actualizado de manera exitosa para el usuario: {}", user.getEmail());
    }

    private void updatePassword(User user, String newPassword) {
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void verifyEmail(String token) {
        log.info("Iniciando validación de correo electrónico mediante token");

        VerificationToken verificationToken = verificationTokenRepository.findByToken(token)
                .orElseThrow(() -> {
                    log.warn("El token de verificación de email provisto no existe");
                    return new ResourceNotFoundException("Token inválido o no encontrado");
                });

        if (verificationToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            log.warn("El token de verificación para el email ha expirado");
            throw new IllegalStateException("El enlace de verificación ha expirado");
        }

        User user = verificationToken.getUser();
        user.setEnabled(true);
        userRepository.save(user);

        verificationTokenRepository.delete(verificationToken);
        log.info("La cuenta para el usuario {} ha sido activada y el token de verificación fue removido", user.getEmail());
    }

    private AuthResponse buildAuthResponse(User user, String token) {
        return new AuthResponse(
                user.getId(),
                token,
                user.getEmail(),
                user.getFullName(),
                user.getRole().name(),
                user.getImage()
        );
    }
}