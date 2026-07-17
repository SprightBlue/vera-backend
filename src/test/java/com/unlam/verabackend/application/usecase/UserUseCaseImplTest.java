package com.unlam.verabackend.application.usecase;

import com.unlam.verabackend.application.service.JwtService;
import com.unlam.verabackend.domain.exception.ResourceNotFoundException;
import com.unlam.verabackend.domain.model.Role;
import com.unlam.verabackend.domain.port.out.EmailProvider;
import com.unlam.verabackend.infrastructure.entity.PasswordResetToken;
import com.unlam.verabackend.infrastructure.entity.User;
import com.unlam.verabackend.infrastructure.entity.VerificationToken;
import com.unlam.verabackend.infrastructure.repository.PasswordResetTokenRepository;
import com.unlam.verabackend.infrastructure.repository.UserRepository;
import com.unlam.verabackend.infrastructure.repository.VerificationTokenRepository;
import com.unlam.verabackend.presentation.dto.AuthResponse;
import com.unlam.verabackend.presentation.dto.LoginRequest;
import com.unlam.verabackend.presentation.dto.RegisterRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Pruebas Unitarias para UserUseCaseImpl")
class UserUseCaseImplTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtService jwtService;
    @Mock private AuthenticationManager authenticationManager;
    @Mock private PasswordResetTokenRepository tokenRepository;
    @Mock private VerificationTokenRepository verificationTokenRepository;
    @Mock private EmailProvider emailProvider;

    @InjectMocks
    private UserUseCaseImpl userUseCase;

    @BeforeEach
    void setUp() {
        String GOOGLE_CLIENT_ID = "mock-google-client-id";
        ReflectionTestUtils.setField(userUseCase, "googleClientId", GOOGLE_CLIENT_ID);
    }

    @Nested
    @DisplayName("Pruebas para register")
    class RegisterTests {

        private RegisterRequest registerRequest;

        @BeforeEach
        void setUp() {
            registerRequest = new RegisterRequest();
            registerRequest.setEmail("test@unlam.edu.ar");
            registerRequest.setFullName("Marta Gómez");
            registerRequest.setPassword("securePassword123");
            registerRequest.setRole("CARER");
            registerRequest.setAcceptedTerms(true);
        }

        @Test
        @DisplayName("Debería registrar al usuario exitosamente y enviar correo de verificación")
        void register_Success_ShouldSaveUserAndSendEmail() throws Exception {
            User savedUser = User.builder()
                    .id(10L)
                    .fullName(registerRequest.getFullName())
                    .email(registerRequest.getEmail())
                    .role(Role.CARER)
                    .enabled(false)
                    .build();

            when(userRepository.existsByEmail(registerRequest.getEmail())).thenReturn(false);
            when(passwordEncoder.encode(registerRequest.getPassword())).thenReturn("encoded_pass");
            when(userRepository.saveAndFlush(any(User.class))).thenReturn(savedUser);

            AuthResponse response = userUseCase.register(registerRequest);

            assertNotNull(response);
            assertEquals(savedUser.getId(), response.getId());
            assertEquals(savedUser.getEmail(), response.getEmail());
            assertNull(response.getToken());

            verify(userRepository, times(1)).saveAndFlush(any(User.class));
            verify(verificationTokenRepository, times(1)).save(any(VerificationToken.class));
            verify(emailProvider, times(1)).sendVerificationEmail(eq(savedUser.getEmail()), anyString());
        }

        @Test
        @DisplayName("Debería lanzar IllegalArgumentException si no se aceptan los términos")
        void register_TermsNotAccepted_ShouldThrowException() {
            registerRequest.setAcceptedTerms(false);

            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                    userUseCase.register(registerRequest)
            );
            assertEquals("Debe aceptar los términos y condiciones", ex.getMessage());
            verifyNoInteractions(userRepository, verificationTokenRepository, emailProvider);
        }

        @Test
        @DisplayName("Debería lanzar IllegalStateException si el correo ya está en uso")
        void register_EmailAlreadyExists_ShouldThrowException() {
            when(userRepository.existsByEmail(registerRequest.getEmail())).thenReturn(true);

            IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                    userUseCase.register(registerRequest)
            );
            assertEquals("El correo electrónico ya está registrado", ex.getMessage());
            verify(userRepository, never()).saveAndFlush(any(User.class));
        }
    }

    @Nested
    @DisplayName("Pruebas para login")
    class LoginTests {

        private LoginRequest loginRequest;
        private User activeUser;

        @BeforeEach
        void setUp() {
            loginRequest = new LoginRequest();
            loginRequest.setEmail("test@unlam.edu.ar");
            loginRequest.setPassword("password123");

            activeUser = User.builder()
                    .id(1L)
                    .email("test@unlam.edu.ar")
                    .fullName("Juan Pérez")
                    .role(Role.CARER)
                    .enabled(true)
                    .build();
        }

        @Test
        @DisplayName("Debería loguear exitosamente y devolver token JWT")
        void login_Success_ShouldReturnToken() {
            String mockJwt = "jwt.token.mock";
            when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.of(activeUser));
            when(jwtService.generateToken(activeUser)).thenReturn(mockJwt);

            AuthResponse response = userUseCase.login(loginRequest);

            assertNotNull(response);
            assertEquals(mockJwt, response.getToken());
            assertEquals(activeUser.getEmail(), response.getEmail());
            verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
        }

        @Test
        @DisplayName("Debería lanzar ResourceNotFoundException si el usuario no existe")
        void login_UserNotFound_ShouldThrowException() {
            when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class, () ->
                    userUseCase.login(loginRequest)
            );
        }

        @Test
        @DisplayName("Debería lanzar IllegalStateException si la cuenta no está verificada")
        void login_UserNotEnabled_ShouldThrowException() {
            activeUser.setEnabled(false);
            when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.of(activeUser));

            IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                    userUseCase.login(loginRequest)
            );
            assertEquals("La cuenta de usuario no está verificada.", ex.getMessage());
        }
    }

    @Nested
    @DisplayName("Pruebas para forgotPassword")
    class ForgotPasswordTests {

        private final String email = "recuperar@unlam.edu.ar";

        @Test
        @DisplayName("Debería generar token de reinicio y enviar email si el usuario existe")
        void forgotPassword_UserExists_ShouldGenerateTokenAndSendEmail() throws Exception {
            User user = User.builder().email(email).build();
            when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

            userUseCase.forgotPassword(email);

            verify(tokenRepository, times(1)).save(any(PasswordResetToken.class));
            verify(emailProvider, times(1)).sendPasswordResetEmail(eq(email), anyString());
        }

        @Test
        @DisplayName("Debería lanzar ResourceNotFoundException si el usuario no existe")
        void forgotPassword_UserNotFound_ShouldThrowException() {
            when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class, () ->
                    userUseCase.forgotPassword(email)
            );
            verifyNoInteractions(tokenRepository, emailProvider);
        }
    }

    @Nested
    @DisplayName("Pruebas para resetPassword")
    class ResetPasswordTests {

        private final String token = "valid-reset-token";
        private final String newPassword = "newSecurePassword";
        private PasswordResetToken resetToken;
        private User user;

        @BeforeEach
        void setUp() {
            user = User.builder()
                    .email("test@unlam.edu.ar")
                    .password("old_encoded_password")
                    .build();

            resetToken = new PasswordResetToken();
            resetToken.setToken(token);
            resetToken.setUser(user);
            resetToken.setUsed(false);
            resetToken.setExpiresAt(LocalDateTime.now().plusHours(1));
        }

        @Test
        @DisplayName("Debería reestablecer la contraseña correctamente")
        void resetPassword_Success_ShouldUpdatePasswordAndMarkTokenAsUsed() {
            when(tokenRepository.findByToken(token)).thenReturn(Optional.of(resetToken));
            when(passwordEncoder.encode(newPassword)).thenReturn("new_encoded_password");

            userUseCase.resetPassword(token, newPassword);

            assertTrue(resetToken.isUsed());
            assertEquals("new_encoded_password", user.getPassword());
            verify(userRepository, times(1)).save(user);
            verify(tokenRepository, times(1)).save(resetToken);
        }

        @Test
        @DisplayName("Debería lanzar ResourceNotFoundException si el token no existe")
        void resetPassword_TokenNotFound_ShouldThrowException() {
            when(tokenRepository.findByToken(token)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class, () ->
                    userUseCase.resetPassword(token, newPassword)
            );
        }

        @Test
        @DisplayName("Debería lanzar IllegalStateException si el token ya fue utilizado")
        void resetPassword_TokenAlreadyUsed_ShouldThrowException() {
            resetToken.setUsed(true);
            when(tokenRepository.findByToken(token)).thenReturn(Optional.of(resetToken));

            IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                    userUseCase.resetPassword(token, newPassword)
            );
            assertEquals("Token ya utilizado", ex.getMessage());
        }

        @Test
        @DisplayName("Debería lanzar IllegalStateException si el token expiró")
        void resetPassword_TokenExpired_ShouldThrowException() {
            resetToken.setExpiresAt(LocalDateTime.now().minusMinutes(1));
            when(tokenRepository.findByToken(token)).thenReturn(Optional.of(resetToken));

            IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                    userUseCase.resetPassword(token, newPassword)
            );
            assertEquals("Token expirado", ex.getMessage());
        }
    }

    @Nested
    @DisplayName("Pruebas para verifyEmail")
    class VerifyEmailTests {

        private final String token = "verification-token-123";
        private VerificationToken verificationToken;
        private User user;

        @BeforeEach
        void setUp() {
            user = User.builder()
                    .email("user@unlam.edu.ar")
                    .enabled(false)
                    .build();

            verificationToken = new VerificationToken(token, user);
            verificationToken.setExpiryDate(LocalDateTime.now().plusDays(1));
        }

        @Test
        @DisplayName("Debería habilitar al usuario y borrar el token de verificación")
        void verifyEmail_Success_ShouldEnableUserAndDeleteToken() {
            when(verificationTokenRepository.findByToken(token)).thenReturn(Optional.of(verificationToken));

            userUseCase.verifyEmail(token);

            assertTrue(user.isEnabled());
            verify(userRepository, times(1)).save(user);
            verify(verificationTokenRepository, times(1)).delete(verificationToken);
        }

        @Test
        @DisplayName("Debería lanzar ResourceNotFoundException si el token no existe")
        void verifyEmail_TokenNotFound_ShouldThrowException() {
            when(verificationTokenRepository.findByToken(token)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class, () ->
                    userUseCase.verifyEmail(token)
            );
        }

        @Test
        @DisplayName("Debería lanzar IllegalStateException si el token expiró")
        void verifyEmail_TokenExpired_ShouldThrowException() {
            verificationToken.setExpiryDate(LocalDateTime.now().minusMinutes(5));
            when(verificationTokenRepository.findByToken(token)).thenReturn(Optional.of(verificationToken));

            IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                    userUseCase.verifyEmail(token)
            );
            assertEquals("El enlace de verificación ha expirado", ex.getMessage());
            assertFalse(user.isEnabled());
            verify(userRepository, never()).save(user);
        }
    }
}