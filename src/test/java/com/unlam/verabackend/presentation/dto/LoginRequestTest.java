package com.unlam.verabackend.presentation.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class LoginRequestTest {

    @Test
    void deberiaAsignarEmailCorrectamente() {
        LoginRequest request = new LoginRequest();

        request.setEmail("angel@gmail.com");

        assertEquals("angel@gmail.com", request.getEmail());
    }

    @Test
    void deberiaAsignarPasswordCorrectamente() {
        LoginRequest request = new LoginRequest();

        request.setPassword("Test1234");

        assertEquals("Test1234", request.getPassword());
    }

    @Test
    void emailNoDeberiaSerNuloDespuesDeAsignarse() {
        LoginRequest request = new LoginRequest();

        request.setEmail("angel@gmail.com");

        assertNotNull(request.getEmail());
    }

    @Test
    void passwordNoDeberiaSerNulaDespuesDeAsignarse() {
        LoginRequest request = new LoginRequest();

        request.setPassword("Test1234");

        assertNotNull(request.getPassword());
    }

    @Test
    void deberiaPermitirModificarEmail() {
        LoginRequest request = new LoginRequest();

        request.setEmail("viejo@gmail.com");
        request.setEmail("nuevo@gmail.com");

        assertEquals("nuevo@gmail.com", request.getEmail());
    }

    @Test
    void deberiaPermitirModificarPassword() {
        LoginRequest request = new LoginRequest();

        request.setPassword("123456");
        request.setPassword("Test1234");

        assertEquals("Test1234", request.getPassword());
    }

    @Test
    void deberiaCrearObjetoVacioInicialmente() {
        LoginRequest request = new LoginRequest();

        assertNull(request.getEmail());
        assertNull(request.getPassword());
    }

    @Test
    void emailPodriaContenerArroba() {
        LoginRequest request = new LoginRequest();

        request.setEmail("angel@gmail.com");

        assertTrue(request.getEmail().contains("@"));
    }

    @Test
    void passwordPodriaTenerLongitudMayorACinco() {
        LoginRequest request = new LoginRequest();

        request.setPassword("Test1234");

        assertTrue(request.getPassword().length() > 5);
    }

    @Test
    void emailNoDeberiaEstarVacioDespuesDeAsignarse() {
        LoginRequest request = new LoginRequest();

        request.setEmail("angel@gmail.com");

        assertFalse(request.getEmail().isEmpty());
    }
}