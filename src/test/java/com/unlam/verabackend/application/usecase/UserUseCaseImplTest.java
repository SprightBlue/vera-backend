package com.unlam.verabackend.application.usecase;

import com.unlam.verabackend.infrastructure.provider.CloudinaryFileCloudAdapter;
import com.unlam.verabackend.domain.port.out.EmailService;
import com.unlam.verabackend.application.service.JwtService;
import com.unlam.verabackend.domain.model.Role;
import com.unlam.verabackend.infrastructure.entity.PasswordResetToken;
import com.unlam.verabackend.infrastructure.entity.User;
import com.unlam.verabackend.infrastructure.entity.VerificationToken;
import com.unlam.verabackend.infrastructure.repository.PasswordResetTokenRepository;
import com.unlam.verabackend.infrastructure.repository.TrustContactRepository;
import com.unlam.verabackend.infrastructure.repository.UserRepository;
import com.unlam.verabackend.infrastructure.repository.VerificationTokenRepository;
import com.unlam.verabackend.presentation.dto.*;
import org.junit.jupiter.api.BeforeEach;
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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserUseCaseImplTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtService jwtService;
    @Mock private AuthenticationManager authenticationManager;
    @Mock private PasswordResetTokenRepository tokenRepository;
    @Mock private VerificationTokenRepository verificationTokenRepository;
    @Mock private EmailService emailService;
    @Mock private CloudinaryFileCloudAdapter cloudinaryFileCloudAdapter;
    @Mock private TrustContactRepository trustContactRepository;

    @InjectMocks
    private UserUseCaseImpl useCase;

    private User usuarioBase;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(useCase, "googleClientId", "mock-client-id");

        usuarioBase = new User();
        ReflectionTestUtils.setField(usuarioBase, "id", 1L);
        usuarioBase.setEmail("tomas@gmail.com");
        usuarioBase.setFullName("Tomas Perez");
        usuarioBase.setPassword("hashedPassword");
        usuarioBase.setRole(Role.CARER);
    }

    @Test
    void deberiaLanzarExcepcionAlRegistrarSiNoAceptaTerminos() {
        RegisterRequest request = mock(RegisterRequest.class);
        when(request.getAcceptedTerms()).thenReturn(false);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            useCase.register(request);
        });
        assertEquals("Debe aceptar los términos y condiciones", exception.getMessage());
    }

    @Test
    void deberiaRegistrarUsuarioConExito() {
        RegisterRequest request = mock(RegisterRequest.class);
        when(request.getAcceptedTerms()).thenReturn(true);
        when(request.getEmail()).thenReturn("nuevo@gmail.com");
        when(request.getPassword()).thenReturn("123456");
        when(request.getFullName()).thenReturn("Nuevo User");
        when(request.getRole()).thenReturn("CARER");

        when(userRepository.existsByEmail("nuevo@gmail.com")).thenReturn(false);
        when(passwordEncoder.encode("123456")).thenReturn("hashed");

        AuthResponse response = useCase.register(request);

        assertNotNull(response);
        assertEquals("nuevo@gmail.com", response.getEmail());
        verify(userRepository, times(1)).save(any(User.class));
        verify(verificationTokenRepository, times(1)).save(any(VerificationToken.class));
        verify(emailService, times(1)).sendVerificationEmail(eq("nuevo@gmail.com"), anyString());
    }

    @Test
    void deberiaLoguearConExitoYDevolverToken() {
        LoginRequest request = mock(LoginRequest.class);
        when(request.getEmail()).thenReturn("tomas@gmail.com");
        when(request.getPassword()).thenReturn("123456");

        when(userRepository.findByEmail("tomas@gmail.com")).thenReturn(Optional.of(usuarioBase));
        when(jwtService.generateToken(usuarioBase)).thenReturn("mock-jwt-token");

        AuthResponse response = useCase.login(request);

        assertEquals("mock-jwt-token", response.getToken());
        verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    void deberiaFallarLoginGoogleConTokenInvalido() {
        Exception exception = assertThrows(Exception.class, () -> {
            useCase.googleLogin("token-falso-123", "CARER");
        });
        assertTrue(exception instanceof IllegalArgumentException || exception instanceof RuntimeException);
    }

    @Test
    void deberiaGenerarTokenDeRecuperacionYEnviarEmail() {
        when(userRepository.findByEmail(usuarioBase.getEmail())).thenReturn(Optional.of(usuarioBase));

        useCase.forgotPassword(usuarioBase.getEmail());

        verify(tokenRepository, times(1)).save(any(PasswordResetToken.class));
        verify(emailService, times(1)).sendPasswordResetEmail(eq(usuarioBase.getEmail()), anyString());
    }

    @Test
    void deberiaLanzarExcepcionAlRestablecerConTokenExpirado() {
        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setUsed(false);
        resetToken.setExpiresAt(LocalDateTime.now().minusHours(1));

        when(tokenRepository.findByToken("token-vencido")).thenReturn(Optional.of(resetToken));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            useCase.resetPassword("token-vencido", "nuevaClave");
        });
        assertEquals("Token expirado", exception.getMessage());
    }

    @Test
    void deberiaRestablecerContrasenaConExito() {
        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setUsed(false);
        resetToken.setExpiresAt(LocalDateTime.now().plusHours(1));
        resetToken.setUser(usuarioBase);

        when(tokenRepository.findByToken("token-valido")).thenReturn(Optional.of(resetToken));
        when(passwordEncoder.encode("nuevaClave")).thenReturn("newHashed");

        useCase.resetPassword("token-valido", "nuevaClave");

        verify(userRepository, times(1)).save(usuarioBase);
        assertTrue(resetToken.isUsed());
        verify(tokenRepository, times(1)).save(resetToken);
    }

    @Test
    void deberiaVerificarEmailYHabilitarUsuario() {
        VerificationToken tokenObj = new VerificationToken("mi-token", usuarioBase);
        tokenObj.setExpiryDate(LocalDateTime.now().plusDays(1));

        when(verificationTokenRepository.findByToken("mi-token")).thenReturn(Optional.of(tokenObj));

        useCase.verifyEmail("mi-token");

        assertTrue(usuarioBase.isEnabled());
        verify(userRepository, times(1)).save(usuarioBase);
        verify(verificationTokenRepository, times(1)).delete(tokenObj);
    }

    @Test
    void deberiaLanzarExcepcionAlRestablecerSiTokenNoExiste() {
        when(tokenRepository.findByToken("token-falso")).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            useCase.resetPassword("token-falso", "nuevaClave");
        });
        assertEquals("Token inválido", exception.getMessage());
    }

    @Test
    void deberiaLanzarExcepcionAlRestablecerSiTokenYaFueUtilizado() {
        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setUsed(true);

        when(tokenRepository.findByToken("token-usado")).thenReturn(Optional.of(resetToken));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            useCase.resetPassword("token-usado", "nuevaClave");
        });
        assertEquals("Token ya utilizado", exception.getMessage());
    }
}