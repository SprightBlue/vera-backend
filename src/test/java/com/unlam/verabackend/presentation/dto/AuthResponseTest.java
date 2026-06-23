package com.unlam.verabackend.presentation.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class AuthResponseTest {

    @Test
    void deberiaCrearAuthResponseCorrectamente() {
        AuthResponse response = new AuthResponse(
                "token123",
                "angel@gmail.com",
                "Angel Leyes",
                "ROLE_USER"
        );

        assertNotNull(response);
    }

    @Test
    void deberiaGuardarTokenCorrectamente() {
        AuthResponse response = new AuthResponse(
                "token123",
                "angel@gmail.com",
                "Angel Leyes",
                "ROLE_USER"
        );

        assertEquals("token123", response.getToken());
    }

    @Test
    void deberiaGuardarEmailCorrectamente() {
        AuthResponse response = new AuthResponse(
                "token123",
                "angel@gmail.com",
                "Angel Leyes",
                "ROLE_USER"
        );

        assertEquals("angel@gmail.com", response.getEmail());
    }

    @Test
    void deberiaGuardarNombreCompletoCorrectamente() {
        AuthResponse response = new AuthResponse(
                "token123",
                "angel@gmail.com",
                "Angel Leyes",
                "ROLE_USER"
        );

        assertEquals("Angel Leyes", response.getFullName());
    }

    @Test
    void deberiaGuardarRolCorrectamente() {
        AuthResponse response = new AuthResponse(
                "token123",
                "angel@gmail.com",
                "Angel Leyes",
                "ROLE_USER"
        );

        assertEquals("ROLE_USER", response.getRole());
    }

    @Test
    void deberiaModificarToken() {
        AuthResponse response = new AuthResponse(
                "token123",
                "angel@gmail.com",
                "Angel Leyes",
                "ROLE_USER"
        );

        response.setToken("nuevoToken");

        assertEquals("nuevoToken", response.getToken());
    }

    @Test
    void deberiaModificarEmail() {
        AuthResponse response = new AuthResponse(
                "token123",
                "angel@gmail.com",
                "Angel Leyes",
                "ROLE_USER"
        );

        response.setEmail("nuevo@gmail.com");

        assertEquals("nuevo@gmail.com", response.getEmail());
    }

    @Test
    void deberiaModificarNombreCompleto() {
        AuthResponse response = new AuthResponse(
                "token123",
                "angel@gmail.com",
                "Angel Leyes",
                "ROLE_USER"
        );

        response.setFullName("Nuevo Nombre");

        assertEquals("Nuevo Nombre", response.getFullName());
    }

    @Test
    void deberiaModificarRol() {
        AuthResponse response = new AuthResponse(
                "token123",
                "angel@gmail.com",
                "Angel Leyes",
                "ROLE_USER"
        );

        response.setRole("ROLE_ADMIN");

        assertEquals("ROLE_ADMIN", response.getRole());
    }

    @Test
    void tokenNoDeberiaSerNulo() {
        AuthResponse response = new AuthResponse(
                "token123",
                "angel@gmail.com",
                "Angel Leyes",
                "ROLE_USER"
        );

        assertNotNull(response.getToken());
    }
}