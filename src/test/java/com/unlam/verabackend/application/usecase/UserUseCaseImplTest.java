package com.unlam.verabackend.application.usecase;

import com.unlam.verabackend.application.service.CloudinaryService;
import com.unlam.verabackend.application.service.EmailService;
import com.unlam.verabackend.application.service.JwtService;
import com.unlam.verabackend.domain.model.Role;
import com.unlam.verabackend.infrastructure.entity.PasswordResetToken;
import com.unlam.verabackend.infrastructure.entity.TrustContact;
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
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
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
    @Mock private CloudinaryService cloudinaryService;
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
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            useCase.googleLogin("token-falso-123", "CARER");
        });
        assertTrue(exception.getMessage().contains("Error validando usuario Google"));
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
    void deberiaLanzarExcepcionAlCambiarPasswordSiNoCoincidenLasNuevas() {
        ChangePasswordRequest request = mock(ChangePasswordRequest.class);
        when(request.getCurrentPassword()).thenReturn("claveVieja");
        when(request.getNewPassword()).thenReturn("clave1");
        when(request.getConfirmPassword()).thenReturn("clave2");

        when(userRepository.findByEmail(usuarioBase.getEmail())).thenReturn(Optional.of(usuarioBase));
        when(passwordEncoder.matches("claveVieja", usuarioBase.getPassword())).thenReturn(true);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            useCase.changePassword(usuarioBase.getEmail(), request);
        });
        assertEquals("Las contraseñas no coinciden", exception.getMessage());
    }

    @Test
    void deberiaCambiarContrasenaConExito() {
        ChangePasswordRequest request = mock(ChangePasswordRequest.class);
        when(request.getCurrentPassword()).thenReturn("claveVieja");
        when(request.getNewPassword()).thenReturn("claveNueva");
        when(request.getConfirmPassword()).thenReturn("claveNueva");

        when(userRepository.findByEmail(usuarioBase.getEmail())).thenReturn(Optional.of(usuarioBase));
        when(passwordEncoder.matches("claveVieja", usuarioBase.getPassword())).thenReturn(true);
        when(passwordEncoder.matches("claveNueva", usuarioBase.getPassword())).thenReturn(false);
        when(passwordEncoder.encode("claveNueva")).thenReturn("hashNuevo");

        useCase.changePassword(usuarioBase.getEmail(), request);

        verify(userRepository, times(1)).save(usuarioBase);
    }

    @Test
    void deberiaCambiarEmailConExito() {
        ChangeEmailRequest request = mock(ChangeEmailRequest.class);
        when(request.getPassword()).thenReturn("miClave");
        when(request.getNewEmail()).thenReturn("nuevo-email@gmail.com");

        when(userRepository.findByEmail(usuarioBase.getEmail())).thenReturn(Optional.of(usuarioBase));
        when(passwordEncoder.matches("miClave", usuarioBase.getPassword())).thenReturn(true);
        when(userRepository.existsByEmail("nuevo-email@gmail.com")).thenReturn(false);

        useCase.changeEmail(usuarioBase.getEmail(), request);

        assertEquals("nuevo-email@gmail.com", usuarioBase.getEmail());
        verify(userRepository, times(1)).save(usuarioBase);
    }


    @Test
    void deberiaObtenerYActualizarPerfil() {
        when(userRepository.findByEmail(usuarioBase.getEmail())).thenReturn(Optional.of(usuarioBase));
        
        ProfileResponse perfil = useCase.getProfile(usuarioBase.getEmail());
        assertEquals(usuarioBase.getFullName(), perfil.getFullName());

        UpdateProfileRequest updateReq = mock(UpdateProfileRequest.class);
        when(updateReq.getFullName()).thenReturn("Nombre Editado");
        when(updateReq.getPhone()).thenReturn("112233");
        when(updateReq.getCountry()).thenReturn("Argentina");

        ProfileResponse actualizado = useCase.updateProfile(usuarioBase.getEmail(), updateReq);

        assertEquals("Nombre Editado", actualizado.getFullName());
        verify(userRepository, times(1)).save(usuarioBase);
    }

    @Test
    void deberiaSubirImagenDeUsuario() throws IOException {
        MultipartFile archivo = mock(MultipartFile.class);
        when(userRepository.findByEmail(usuarioBase.getEmail())).thenReturn(Optional.of(usuarioBase));
        when(cloudinaryService.uploadImage(archivo, "users")).thenReturn("http://nube.com/foto.jpg");

        UploadImageResponse response = useCase.uploadUserImage(usuarioBase.getEmail(), archivo);

        assertEquals("http://nube.com/foto.jpg", response.getImage());
        verify(userRepository, times(1)).save(usuarioBase);
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
    void deberiaLanzarExcepcionAlEliminarCuentaSiTieneProtegidos() {
        when(userRepository.findByEmail(usuarioBase.getEmail())).thenReturn(Optional.of(usuarioBase));
        
        when(trustContactRepository.findByCarerId(usuarioBase.getId())).thenReturn(List.of(new TrustContact()));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            useCase.deleteAccount(usuarioBase.getEmail());
        });
        assertTrue(exception.getMessage().contains("No puedes eliminar tu cuenta porque todavía tienes personas protegidas"));
    }

    @Test
    void deberiaEliminarCuentaConExitoSiEstaLimpio() {
        when(userRepository.findByEmail(usuarioBase.getEmail())).thenReturn(Optional.of(usuarioBase));
        when(trustContactRepository.findByCarerId(usuarioBase.getId())).thenReturn(List.of());

        useCase.deleteAccount(usuarioBase.getEmail());

        verify(userRepository, times(1)).delete(usuarioBase);
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