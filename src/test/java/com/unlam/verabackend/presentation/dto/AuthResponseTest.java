package com.unlam.verabackend.presentation.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class AuthResponseTest {

    @Test
    void deberiaCrearAuthResponseCorrectamente() {
        AuthResponse response = new AuthResponse(
                1L,
                "token123",
                "angel@gmail.com",
                "Angel Leyes",
                "CARER"
        );

        assertNotNull(response);
    }

    @Test
    void deberiaGuardarIdCorrectamente() {
        AuthResponse response = new AuthResponse(
                1L,
                "token123",
                "angel@gmail.com",
                "Angel Leyes",
                "CARER"
        );

        assertEquals(1L, response.getId());
    }

    @Test
    void deberiaGuardarTokenCorrectamente() {
        AuthResponse response = new AuthResponse(
                1L,
                "token123",
                "angel@gmail.com",
                "Angel Leyes",
                "CARER"
        );

        assertEquals("token123", response.getToken());
    }

    @Test
    void deberiaGuardarEmailCorrectamente() {
        AuthResponse response = new AuthResponse(
                1L,
                "token123",
                "angel@gmail.com",
                "Angel Leyes",
                "CARER"
        );

        assertEquals("angel@gmail.com", response.getEmail());
    }

    @Test
    void deberiaGuardarNombreCompletoCorrectamente() {
        AuthResponse response = new AuthResponse(
                1L,
                "token123",
                "angel@gmail.com",
                "Angel Leyes",
                "CARER"
        );

        assertEquals("Angel Leyes", response.getFullName());
    }

    @Test
    void deberiaGuardarRolCorrectamente() {
        AuthResponse response = new AuthResponse(
                1L,
                "token123",
                "angel@gmail.com",
                "Angel Leyes",
                "CARER"
        );

        assertEquals("CARER", response.getRole());
    }

    @Test
    void deberiaModificarId() {
        AuthResponse response = new AuthResponse(
                1L,
                "token123",
                "angel@gmail.com",
                "Angel Leyes",
                "CARER"
        );

        response.setId(2L);

        assertEquals(2L, response.getId());
    }

    @Test
    void deberiaModificarToken() {
        AuthResponse response = new AuthResponse(
                1L,
                "token123",
                "angel@gmail.com",
                "Angel Leyes",
                "CARER"
        );

        response.setToken("nuevoToken");

        assertEquals("nuevoToken", response.getToken());
    }

    @Test
    void deberiaModificarEmail() {
        AuthResponse response = new AuthResponse(
                1L,
                "token123",
                "angel@gmail.com",
                "Angel Leyes",
                "CARER"
        );

        response.setEmail("nuevo@gmail.com");

        assertEquals("nuevo@gmail.com", response.getEmail());
    }

    @Test
    void deberiaModificarNombreCompleto() {
        AuthResponse response = new AuthResponse(
                1L,
                "token123",
                "angel@gmail.com",
                "Angel Leyes",
                "CARER"
        );

        response.setFullName("Nuevo Nombre");

        assertEquals("Nuevo Nombre", response.getFullName());
    }

    @Test
    void deberiaModificarRol() {
        AuthResponse response = new AuthResponse(
                1L,
                "token123",
                "angel@gmail.com",
                "Angel Leyes",
                "CARER"
        );

        response.setRole("ADMIN");

        assertEquals("ADMIN", response.getRole());
    }

    @Test
    void tokenNoDeberiaSerNulo() {
        AuthResponse response = new AuthResponse(
                1L,
                "token123",
                "angel@gmail.com",
                "Angel Leyes",
                "CARER"
        );

        assertNotNull(response.getToken());
    }
}