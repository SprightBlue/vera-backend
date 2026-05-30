package com.unlam.verabackend.domain.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class RoleTest {

    @Test
    void deberiaExistirRoleUser() {
        assertNotNull(Role.ROLE_USER);
    }

    @Test
    void deberiaExistirRoleAdmin() {
        assertNotNull(Role.ROLE_ADMIN);
    }

    @Test
    void deberiaConvertirStringARoleUser() {
        Role role = Role.valueOf("ROLE_USER");

        assertEquals(Role.ROLE_USER, role);
    }

    @Test
    void deberiaConvertirStringARoleAdmin() {
        Role role = Role.valueOf("ROLE_ADMIN");

        assertEquals(Role.ROLE_ADMIN, role);
    }

    @Test
    void roleUserDeberiaTenerNombreCorrecto() {
        assertEquals("ROLE_USER", Role.ROLE_USER.name());
    }

    @Test
    void roleAdminDeberiaTenerNombreCorrecto() {
        assertEquals("ROLE_ADMIN", Role.ROLE_ADMIN.name());
    }

    @Test
    void deberiaTenerDosRolesDefinidos() {
        assertEquals(2, Role.values().length);
    }

    @Test
    void noDeberiaAceptarValorInvalido() {
        assertThrows(IllegalArgumentException.class, () ->
                Role.valueOf("ROLE_INVALIDO"));
    }

    @Test
    void roleUserYRoleAdminDeberianSerDistintos() {
        assertNotEquals(Role.ROLE_USER, Role.ROLE_ADMIN);
    }

    @Test
    void valuesDeberiaContenerAmbosRoles() {
        Role[] roles = Role.values();

        assertTrue(java.util.Arrays.asList(roles).contains(Role.ROLE_USER));
        assertTrue(java.util.Arrays.asList(roles).contains(Role.ROLE_ADMIN));
    }
}