package com.unlam.verabackend.application.usecase;

import com.unlam.verabackend.domain.model.Role;
import com.unlam.verabackend.infrastructure.repository.UserRepository;
import com.unlam.verabackend.infrastructure.entity.User;
import com.unlam.verabackend.infrastructure.provider.JwtService;
import com.unlam.verabackend.presentation.dto.AuthResponse;
import com.unlam.verabackend.presentation.dto.LoginRequest;
import com.unlam.verabackend.presentation.dto.RegisterRequest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private UserServiceImpl userService;

    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;
    private User mockUser;

    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequest();
        registerRequest.setFullName("Angel Test");
        registerRequest.setEmail("angeltest@gmail.com");
        registerRequest.setPassword("Test1234");

        loginRequest = new LoginRequest();
        loginRequest.setEmail("angeltest@gmail.com");
        loginRequest.setPassword("Test1234");

        mockUser = new User();
        mockUser.setId(1L);
        mockUser.setFullName("Angel Test");
        mockUser.setEmail("angeltest@gmail.com");
        mockUser.setPassword("encodedPassword");
        mockUser.setRole(Role.ROLE_USER);
    }

    @Test
    void deberiaRegistrarUsuarioCorrectamente() {
        when(userRepository.existsByEmail(registerRequest.getEmail()))
                .thenReturn(false);

        when(passwordEncoder.encode(registerRequest.getPassword()))
                .thenReturn("encodedPassword");

        when(jwtService.generateToken(any(User.class)))
                .thenReturn("fake-jwt-token");

        AuthResponse response = userService.register(registerRequest);

        assertNotNull(response);
        assertEquals("angeltest@gmail.com", response.getEmail());
        assertEquals("Angel Test", response.getFullName());

        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void deberiaLanzarExcepcionSiElEmailYaExiste() {
        when(userRepository.existsByEmail(registerRequest.getEmail()))
                .thenReturn(true);

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> userService.register(registerRequest)
        );

        assertEquals(
                "El correo electrónico ya está registrado",
                exception.getMessage()
        );

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void deberiaIniciarSesionCorrectamente() {
        when(userRepository.findByEmail(loginRequest.getEmail()))
                .thenReturn(Optional.of(mockUser));

        when(jwtService.generateToken(mockUser))
                .thenReturn("fake-jwt-token");

        AuthResponse response = userService.login(loginRequest);

        assertNotNull(response);
        assertEquals(mockUser.getEmail(), response.getEmail());

        verify(authenticationManager, times(1))
                .authenticate(any());
    }

    @Test
    void deberiaLanzarExcepcionSiElUsuarioNoExiste() {
        when(userRepository.findByEmail(loginRequest.getEmail()))
                .thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> userService.login(loginRequest)
        );

        assertEquals(
                "Usuario no encontrado",
                exception.getMessage()
        );
    }

    @Test
    void deberiaLanzarExcepcionSiLasCredencialesSonInvalidas() {
        doThrow(new BadCredentialsException("Bad credentials"))
                .when(authenticationManager)
                .authenticate(any());

        assertThrows(
                BadCredentialsException.class,
                () -> userService.login(loginRequest)
        );
    }

    @Test
void deberiaEncriptarLaContrasenaAlRegistrarUsuario() {
    when(userRepository.existsByEmail(registerRequest.getEmail()))
            .thenReturn(false);

    when(passwordEncoder.encode(registerRequest.getPassword()))
            .thenReturn("encodedPassword");

    when(jwtService.generateToken(any(User.class)))
            .thenReturn("token");

    userService.register(registerRequest);

    verify(passwordEncoder, times(1))
            .encode(registerRequest.getPassword());
}

@Test
void deberiaGenerarTokenAlRegistrarUsuario() {
    when(userRepository.existsByEmail(registerRequest.getEmail()))
            .thenReturn(false);

    when(passwordEncoder.encode(anyString()))
            .thenReturn("encodedPassword");

    when(jwtService.generateToken(any(User.class)))
            .thenReturn("token");

    userService.register(registerRequest);

    verify(jwtService, times(1))
            .generateToken(any(User.class));
}

@Test
void deberiaGenerarTokenAlIniciarSesion() {
    when(userRepository.findByEmail(loginRequest.getEmail()))
            .thenReturn(Optional.of(mockUser));

    when(jwtService.generateToken(mockUser))
            .thenReturn("token");

    userService.login(loginRequest);

    verify(jwtService, times(1))
            .generateToken(mockUser);
}

@Test
void deberiaBuscarUsuarioPorEmailAlIniciarSesion() {
    when(userRepository.findByEmail(loginRequest.getEmail()))
            .thenReturn(Optional.of(mockUser));

    when(jwtService.generateToken(mockUser))
            .thenReturn("token");

    userService.login(loginRequest);

    verify(userRepository, times(1))
            .findByEmail(loginRequest.getEmail());
}

@Test
void deberiaGuardarUsuarioUnaSolaVez() {
    when(userRepository.existsByEmail(registerRequest.getEmail()))
            .thenReturn(false);

    when(passwordEncoder.encode(anyString()))
            .thenReturn("encodedPassword");

    when(jwtService.generateToken(any(User.class)))
            .thenReturn("token");

    userService.register(registerRequest);

    verify(userRepository, times(1))
            .save(any(User.class));
}

@Test
void noDeberiaGenerarTokenSiElEmailYaExiste() {
    when(userRepository.existsByEmail(registerRequest.getEmail()))
            .thenReturn(true);

    assertThrows(
            RuntimeException.class,
            () -> userService.register(registerRequest)
    );

    verify(jwtService, never())
            .generateToken(any(User.class));
}

@Test
void deberiaAutenticarConEmailYContrasenaCorrectos() {
    when(userRepository.findByEmail(loginRequest.getEmail()))
            .thenReturn(Optional.of(mockUser));

    when(jwtService.generateToken(mockUser))
            .thenReturn("token");

    userService.login(loginRequest);

    verify(authenticationManager).authenticate(
            argThat(auth ->
                    auth.getPrincipal().equals(loginRequest.getEmail()) &&
                    auth.getCredentials().equals(loginRequest.getPassword())
            )
    );
}

@Test
void deberiaRetornarRolUserAlRegistrar() {
    when(userRepository.existsByEmail(registerRequest.getEmail()))
            .thenReturn(false);

    when(passwordEncoder.encode(anyString()))
            .thenReturn("encodedPassword");

    when(jwtService.generateToken(any(User.class)))
            .thenReturn("token");

    AuthResponse response = userService.register(registerRequest);

    assertEquals("ROLE_USER", response.getRole());
}

@Test
void deberiaAsignarRolUserAlCrearUsuario() {
    when(userRepository.existsByEmail(anyString()))
            .thenReturn(false);

    when(passwordEncoder.encode(anyString()))
            .thenReturn("encoded");

    when(jwtService.generateToken(any(User.class)))
            .thenReturn("token");

    userService.register(registerRequest);

    verify(userRepository).save(argThat(user ->
            user.getRole() == Role.ROLE_USER
    ));
}

@Test
void deberiaGuardarEmailCorrecto() {
    when(userRepository.existsByEmail(anyString()))
            .thenReturn(false);

    when(passwordEncoder.encode(anyString()))
            .thenReturn("encoded");

    when(jwtService.generateToken(any(User.class)))
            .thenReturn("token");

    userService.register(registerRequest);

    verify(userRepository).save(argThat(user ->
            user.getEmail().equals(registerRequest.getEmail())
    ));
}

@Test
void deberiaGuardarNombreCompletoCorrecto() {
    when(userRepository.existsByEmail(anyString()))
            .thenReturn(false);

    when(passwordEncoder.encode(anyString()))
            .thenReturn("encoded");

    when(jwtService.generateToken(any(User.class)))
            .thenReturn("token");

    userService.register(registerRequest);

    verify(userRepository).save(argThat(user ->
            user.getFullName().equals(registerRequest.getFullName())
    ));
}

@Test
void noDeberiaGuardarContrasenaSinEncriptar() {
    when(userRepository.existsByEmail(anyString()))
            .thenReturn(false);

    when(passwordEncoder.encode(anyString()))
            .thenReturn("encodedPassword");

    when(jwtService.generateToken(any(User.class)))
            .thenReturn("token");

    userService.register(registerRequest);

    verify(userRepository).save(argThat(user ->
            !user.getPassword().equals(registerRequest.getPassword())
    ));
}

@Test
void loginDeberiaRetornarToken() {
    when(userRepository.findByEmail(anyString()))
            .thenReturn(Optional.of(mockUser));

    when(jwtService.generateToken(mockUser))
            .thenReturn("token-login");

    AuthResponse response = userService.login(loginRequest);

    assertEquals("token-login", response.getToken());
}

@Test
void loginDeberiaRetornarNombreCorrecto() {
    when(userRepository.findByEmail(anyString()))
            .thenReturn(Optional.of(mockUser));

    when(jwtService.generateToken(mockUser))
            .thenReturn("token");

    AuthResponse response = userService.login(loginRequest);

    assertEquals(mockUser.getFullName(), response.getFullName());
}

@Test
void loginDeberiaRetornarEmailCorrecto() {
    when(userRepository.findByEmail(anyString()))
            .thenReturn(Optional.of(mockUser));

    when(jwtService.generateToken(mockUser))
            .thenReturn("token");

    AuthResponse response = userService.login(loginRequest);

    assertEquals(mockUser.getEmail(), response.getEmail());
}

@Test
void noDeberiaGuardarUsuarioSiElEmailYaExiste() {
    when(userRepository.existsByEmail(anyString()))
            .thenReturn(true);

    assertThrows(
            RuntimeException.class,
            () -> userService.register(registerRequest)
    );

    verify(userRepository, never()).save(any(User.class));
}

}