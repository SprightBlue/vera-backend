package com.unlam.verabackend.domain.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class DomainUserTest {

    @Test
    void deberiaCrearDomainUserVacio() {
        DomainUser user = new DomainUser();

        assertNotNull(user);
    }

    @Test
    void deberiaCrearDomainUserConConstructorCompleto() {
        LocalDateTime now = LocalDateTime.now();

        DomainUser user = new DomainUser(
                1L,
                "Angel Leyes",
                "angel@gmail.com",
                Role.ROLE_USER,
                now,
                now,
                true,
                true
        );

        assertNotNull(user);
    }

    @Test
    void deberiaGuardarIdCorrectamente() {
        DomainUser user = new DomainUser();

        user.setId(1L);

        assertEquals(1L, user.getId());
    }

    @Test
    void deberiaGuardarNombreCorrectamente() {
        DomainUser user = new DomainUser();

        user.setFullName("Angel Leyes");

        assertEquals("Angel Leyes", user.getFullName());
    }

    @Test
    void deberiaGuardarEmailCorrectamente() {
        DomainUser user = new DomainUser();

        user.setEmail("angel@gmail.com");

        assertEquals("angel@gmail.com", user.getEmail());
    }

    @Test
    void deberiaGuardarRolCorrectamente() {
        DomainUser user = new DomainUser();

        user.setRole(Role.ROLE_USER);

        assertEquals(Role.ROLE_USER, user.getRole());
    }

    @Test
    void deberiaGuardarCreatedAtCorrectamente() {
        DomainUser user = new DomainUser();
        LocalDateTime now = LocalDateTime.now();

        user.setCreatedAt(now);

        assertEquals(now, user.getCreatedAt());
    }

    @Test
    void deberiaGuardarUpdatedAtCorrectamente() {
        DomainUser user = new DomainUser();
        LocalDateTime now = LocalDateTime.now();

        user.setUpdatedAt(now);

        assertEquals(now, user.getUpdatedAt());
    }

    @Test
    void deberiaGuardarAccountNonLockedCorrectamente() {
        DomainUser user = new DomainUser();

        user.setAccountNonLocked(true);

        assertTrue(user.isAccountNonLocked());
    }

    @Test
    void deberiaGuardarEnabledCorrectamente() {
        DomainUser user = new DomainUser();

        user.setEnabled(true);

        assertTrue(user.isEnabled());
    }

    @Test
    void deberiaPermitirModificarNombre() {
        DomainUser user = new DomainUser();

        user.setFullName("Angel");
        user.setFullName("Angel Leyes");

        assertEquals("Angel Leyes", user.getFullName());
    }

    @Test
    void deberiaPermitirModificarEmail() {
        DomainUser user = new DomainUser();

        user.setEmail("viejo@gmail.com");
        user.setEmail("nuevo@gmail.com");

        assertEquals("nuevo@gmail.com", user.getEmail());
    }

    @Test
    void deberiaPermitirCambiarEstadoEnabled() {
        DomainUser user = new DomainUser();

        user.setEnabled(false);
        user.setEnabled(true);

        assertTrue(user.isEnabled());
    }

    @Test
    void deberiaPermitirCambiarEstadoLocked() {
        DomainUser user = new DomainUser();

        user.setAccountNonLocked(false);
        user.setAccountNonLocked(true);

        assertTrue(user.isAccountNonLocked());
    }
}