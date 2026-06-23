package com.unlam.verabackend.presentation.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class RegisterRequestTest {

    @Test
    void deberiaAsignarNombreCompletoCorrectamente() {
        RegisterRequest request = new RegisterRequest();

        request.setFullName("Angel Leyes");

        assertEquals("Angel Leyes", request.getFullName());
    }

    @Test
    void deberiaAsignarEmailCorrectamente() {
        RegisterRequest request = new RegisterRequest();

        request.setEmail("angel@gmail.com");

        assertEquals("angel@gmail.com", request.getEmail());
    }

    @Test
    void deberiaAsignarPasswordCorrectamente() {
        RegisterRequest request = new RegisterRequest();

        request.setPassword("Test1234");

        assertEquals("Test1234", request.getPassword());
    }

    @Test
    void nombreCompletoNoDeberiaSerNuloDespuesDeAsignarse() {
        RegisterRequest request = new RegisterRequest();

        request.setFullName("Angel");

        assertNotNull(request.getFullName());
    }

    @Test
    void emailNoDeberiaSerNuloDespuesDeAsignarse() {
        RegisterRequest request = new RegisterRequest();

        request.setEmail("angel@gmail.com");

        assertNotNull(request.getEmail());
    }

    @Test
    void passwordNoDeberiaSerNulaDespuesDeAsignarse() {
        RegisterRequest request = new RegisterRequest();

        request.setPassword("Test1234");

        assertNotNull(request.getPassword());
    }

    @Test
    void deberiaPermitirModificarNombreCompleto() {
        RegisterRequest request = new RegisterRequest();

        request.setFullName("Angel");
        request.setFullName("Angel Leyes");

        assertEquals("Angel Leyes", request.getFullName());
    }

    @Test
    void deberiaPermitirModificarEmail() {
        RegisterRequest request = new RegisterRequest();

        request.setEmail("viejo@gmail.com");
        request.setEmail("nuevo@gmail.com");

        assertEquals("nuevo@gmail.com", request.getEmail());
    }

    @Test
    void deberiaPermitirModificarPassword() {
        RegisterRequest request = new RegisterRequest();

        request.setPassword("123456");
        request.setPassword("Test1234");

        assertEquals("Test1234", request.getPassword());
    }

    @Test
    void deberiaCrearObjetoVacioInicialmente() {
        RegisterRequest request = new RegisterRequest();

        assertNull(request.getFullName());
        assertNull(request.getEmail());
        assertNull(request.getPassword());
    }
}