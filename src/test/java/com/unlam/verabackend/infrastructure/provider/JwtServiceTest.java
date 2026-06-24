package com.unlam.verabackend.infrastructure.provider;

import com.unlam.verabackend.application.service.JwtService;
import com.unlam.verabackend.infrastructure.entity.User;
import com.unlam.verabackend.domain.model.Role;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

public class JwtServiceTest {

    private JwtService jwtService;
    private User user;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();

        ReflectionTestUtils.setField(
                jwtService,
                "secretKey",
                "bXlzZWNyZXRrZXkxMjM0NTY3ODkwMTIzNDU2Nzg5MA=="
        );

        ReflectionTestUtils.setField(
                jwtService,
                "jwtExpiration",
                3600000L
        );

        user = new User();
        user.setEmail("angeltest@gmail.com");
        user.setPassword("Test1234");
        user.setFullName("Angel Test");
        user.setRole(Role.CARER);
    }

    @Test
    void deberiaGenerarTokenCorrectamente() {
        String token = jwtService.generateToken(user);

        assertNotNull(token);
        assertFalse(token.isBlank());
    }

    @Test
    void deberiaExtraerUsernameCorrectamente() {
        String token = jwtService.generateToken(user);

        String username = jwtService.extractUsername(token);

        assertEquals("angeltest@gmail.com", username);
    }

    @Test
    void deberiaValidarTokenCorrecto() {
        String token = jwtService.generateToken(user);

        boolean valido = jwtService.isTokenValid(token, user);

        assertTrue(valido);
    }

    @Test
    void deberiaInvalidarTokenConUsuarioIncorrecto() {
        String token = jwtService.generateToken(user);

        User otroUsuario = new User();
        otroUsuario.setEmail("otro@gmail.com");

        boolean valido = jwtService.isTokenValid(token, otroUsuario);

        assertFalse(valido);
    }

    @Test
    void noDeberiaAceptarTokenVacio() {
        assertThrows(Exception.class, () ->
                jwtService.extractUsername(""));
    }

    @Test
    void noDeberiaAceptarTokenInvalido() {
        boolean valido = jwtService.isTokenValid("token-falso", user);

        assertFalse(valido);
    }

    @Test
    void tokenGeneradoDeberiaContenerTresPartes() {
        String token = jwtService.generateToken(user);

        String[] partes = token.split("\\.");

        assertEquals(3, partes.length);
    }

   
    @Test
    void tokenDeberiaContenerElEmailDelUsuario() {
        String token = jwtService.generateToken(user);

        String username = jwtService.extractUsername(token);

        assertEquals(user.getEmail(), username);
    }

    @Test
    void tokenGeneradoNoDeberiaSerNulo() {
        String token = jwtService.generateToken(user);

        assertNotNull(token);
    }

    @Test
    void tokenGeneradoNoDeberiaEstarVacio() {
        String token = jwtService.generateToken(user);

        assertFalse(token.isEmpty());
    }

    @Test
    void deberiaMantenerValidezParaElMismoUsuario() {
        String token = jwtService.generateToken(user);

        assertTrue(jwtService.isTokenValid(token, user));
    }

@Test
void extractUsernameDeberiaFallarConTokenNulo() {
    assertThrows(Exception.class, () ->
            jwtService.extractUsername(null));
}

@Test
void isTokenValidDeberiaRetornarFalseConTokenVacio() {
    boolean valido = jwtService.isTokenValid("", user);

    assertFalse(valido);
}

@Test
void tokenGeneradoDeberiaComenzarConFormatoJwt() {
    String token = jwtService.generateToken(user);

    assertTrue(token.contains("."));
}

@Test
void deberiaGenerarTokenParaUsuarioConRoleAdmin() {
    User admin = new User();
    admin.setEmail("admin@test.com");
    admin.setPassword("Admin123");
    admin.setFullName("Admin");
    admin.setRole(Role.ADMIN);

    String token = jwtService.generateToken(admin);

    assertNotNull(token);
    assertEquals("admin@test.com", jwtService.extractUsername(token));
}

}