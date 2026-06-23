package com.unlam.verabackend.presentation.controller;

import com.unlam.verabackend.domain.port.in.UserUseCase;
import com.unlam.verabackend.presentation.dto.AuthResponse;
import com.unlam.verabackend.presentation.dto.LoginRequest;
import com.unlam.verabackend.presentation.dto.RegisterRequest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthControllerTest {

    @Mock
    private UserUseCase userService;

    @InjectMocks
    private AuthController authController;

    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;
    private AuthResponse authResponse;

    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequest();
        registerRequest.setFullName("Angel Test");
        registerRequest.setEmail("angeltest@gmail.com");
        registerRequest.setPassword("Test1234");

        loginRequest = new LoginRequest();
        loginRequest.setEmail("angeltest@gmail.com");
        loginRequest.setPassword("Test1234");

        authResponse = new AuthResponse(
                "fake-token",
                "angeltest@gmail.com",
                "Angel Test",
                "ROLE_USER"
        );
    }

    @Test
    void deberiaRegistrarUsuarioCorrectamente() {
        when(userService.register(registerRequest))
                .thenReturn(authResponse);

        ResponseEntity<AuthResponse> response =
                authController.register(registerRequest);

        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(authResponse, response.getBody());

        verify(userService, times(1))
                .register(registerRequest);
    }

    @Test
    void deberiaRetornarStatusCreatedAlRegistrar() {
        when(userService.register(registerRequest))
                .thenReturn(authResponse);

        ResponseEntity<AuthResponse> response =
                authController.register(registerRequest);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
    }

    @Test
    void deberiaIniciarSesionCorrectamente() {
        when(userService.login(loginRequest))
                .thenReturn(authResponse);

        ResponseEntity<AuthResponse> response =
                authController.login(loginRequest);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(authResponse, response.getBody());

        verify(userService, times(1))
                .login(loginRequest);
    }

    @Test
    void deberiaRetornarStatusOkAlIniciarSesion() {
        when(userService.login(loginRequest))
                .thenReturn(authResponse);

        ResponseEntity<AuthResponse> response =
                authController.login(loginRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void deberiaRetornarBodyCorrectoAlRegistrar() {
        when(userService.register(registerRequest))
                .thenReturn(authResponse);

        ResponseEntity<AuthResponse> response =
                authController.register(registerRequest);

        assertEquals("angeltest@gmail.com",
                response.getBody().getEmail());
    }

    @Test
    void deberiaRetornarBodyCorrectoAlIniciarSesion() {
        when(userService.login(loginRequest))
                .thenReturn(authResponse);

        ResponseEntity<AuthResponse> response =
                authController.login(loginRequest);

        assertEquals("fake-token",
                response.getBody().getToken());
    }

    @Test
    void deberiaInvocarServicioDeRegistroUnaSolaVez() {
        when(userService.register(registerRequest))
                .thenReturn(authResponse);

        authController.register(registerRequest);

        verify(userService, times(1))
                .register(registerRequest);
    }

    @Test
    void deberiaInvocarServicioDeLoginUnaSolaVez() {
        when(userService.login(loginRequest))
                .thenReturn(authResponse);

        authController.login(loginRequest);

        verify(userService, times(1))
                .login(loginRequest);
    }

    @Test
void deberiaRetornarEmailCorrectoEnRegistro() {
    when(userService.register(registerRequest))
            .thenReturn(authResponse);

    ResponseEntity<AuthResponse> response =
            authController.register(registerRequest);

    assertEquals(
            authResponse.getEmail(),
            response.getBody().getEmail()
    );
}

@Test
void deberiaRetornarNombreCorrectoEnRegistro() {
    when(userService.register(registerRequest))
            .thenReturn(authResponse);

    ResponseEntity<AuthResponse> response =
            authController.register(registerRequest);

    assertEquals(
            authResponse.getFullName(),
            response.getBody().getFullName()
    );
}

@Test
void deberiaRetornarRolCorrectoEnRegistro() {
    when(userService.register(registerRequest))
            .thenReturn(authResponse);

    ResponseEntity<AuthResponse> response =
            authController.register(registerRequest);

    assertEquals(
            "ROLE_USER",
            response.getBody().getRole()
    );
}

@Test
void deberiaRetornarEmailCorrectoEnLogin() {
    when(userService.login(loginRequest))
            .thenReturn(authResponse);

    ResponseEntity<AuthResponse> response =
            authController.login(loginRequest);

    assertEquals(
            authResponse.getEmail(),
            response.getBody().getEmail()
    );
}

@Test
void deberiaRetornarNombreCorrectoEnLogin() {
    when(userService.login(loginRequest))
            .thenReturn(authResponse);

    ResponseEntity<AuthResponse> response =
            authController.login(loginRequest);

    assertEquals(
            authResponse.getFullName(),
            response.getBody().getFullName()
    );
}

@Test
void deberiaRetornarRolCorrectoEnLogin() {
    when(userService.login(loginRequest))
            .thenReturn(authResponse);

    ResponseEntity<AuthResponse> response =
            authController.login(loginRequest);

    assertEquals(
            "ROLE_USER",
            response.getBody().getRole()
    );
}

@Test
void noDeberiaRetornarRespuestaNulaEnRegistro() {
    when(userService.register(registerRequest))
            .thenReturn(authResponse);

    ResponseEntity<AuthResponse> response =
            authController.register(registerRequest);

    assertNotNull(response.getBody());
}

@Test
void noDeberiaRetornarRespuestaNulaEnLogin() {
    when(userService.login(loginRequest))
            .thenReturn(authResponse);

    ResponseEntity<AuthResponse> response =
            authController.login(loginRequest);

    assertNotNull(response.getBody());
}

@Test
void deberiaRetornarTokenCorrectoEnRegistro() {
    when(userService.register(registerRequest))
            .thenReturn(authResponse);

    ResponseEntity<AuthResponse> response =
            authController.register(registerRequest);

    assertEquals(
            authResponse.getToken(),
            response.getBody().getToken()
    );
}

@Test
void deberiaRetornarTokenCorrectoEnLogin() {
    when(userService.login(loginRequest))
            .thenReturn(authResponse);

    ResponseEntity<AuthResponse> response =
            authController.login(loginRequest);

    assertEquals(
            authResponse.getToken(),
            response.getBody().getToken()
    );
}

@Test
void deberiaDelegarElMismoRequestEnRegistro() {
    when(userService.register(any()))
            .thenReturn(authResponse);

    authController.register(registerRequest);

    verify(userService).register(same(registerRequest));
}

@Test
void deberiaDelegarElMismoRequestEnLogin() {
    when(userService.login(any()))
            .thenReturn(authResponse);

    authController.login(loginRequest);

    verify(userService).login(same(loginRequest));
}

@Test
void registerDeberiaRetornarResponseEntity() {
    when(userService.register(registerRequest))
            .thenReturn(authResponse);

    ResponseEntity<AuthResponse> response =
            authController.register(registerRequest);

    assertInstanceOf(ResponseEntity.class, response);
}

@Test
void loginDeberiaRetornarResponseEntity() {
    when(userService.login(loginRequest))
            .thenReturn(authResponse);

    ResponseEntity<AuthResponse> response =
            authController.login(loginRequest);

    assertInstanceOf(ResponseEntity.class, response);
}

@Test
void registerNoDeberiaInvocarLogin() {
    when(userService.register(registerRequest))
            .thenReturn(authResponse);

    authController.register(registerRequest);

    verify(userService, never()).login(any());
}

@Test
void loginNoDeberiaInvocarRegister() {
    when(userService.login(loginRequest))
            .thenReturn(authResponse);

    authController.login(loginRequest);

    verify(userService, never()).register(any());
}

@Test
void deberiaRetornarRespuestaCompletaEnRegistro() {
    when(userService.register(registerRequest))
            .thenReturn(authResponse);

    ResponseEntity<AuthResponse> response =
            authController.register(registerRequest);

    assertAll(
            () -> assertEquals("fake-token", response.getBody().getToken()),
            () -> assertEquals("angeltest@gmail.com", response.getBody().getEmail()),
            () -> assertEquals("Angel Test", response.getBody().getFullName()),
            () -> assertEquals("ROLE_USER", response.getBody().getRole())
    );
}

@Test
void deberiaRetornarRespuestaCompletaEnLogin() {
    when(userService.login(loginRequest))
            .thenReturn(authResponse);

    ResponseEntity<AuthResponse> response =
            authController.login(loginRequest);

    assertAll(
            () -> assertEquals("fake-token", response.getBody().getToken()),
            () -> assertEquals("angeltest@gmail.com", response.getBody().getEmail()),
            () -> assertEquals("Angel Test", response.getBody().getFullName()),
            () -> assertEquals("ROLE_USER", response.getBody().getRole())
    );
}
}