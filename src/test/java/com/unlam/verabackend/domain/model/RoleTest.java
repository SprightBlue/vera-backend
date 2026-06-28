package com.unlam.verabackend.domain.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class RoleTest {

    @Test
    void deberiaExistirRoleCarer() {
        assertNotNull(Role.CARER);
    }

    @Test
    void deberiaExistirRoleProtected() {
        assertNotNull(Role.PROTECTED);
    }

    @Test
    void deberiaExistirRoleAdmin() {
        assertNotNull(Role.ADMIN);
    }

    @Test
    void deberiaConvertirStringARoleCarer() {
        Role role = Role.valueOf("CARER");
        assertEquals(Role.CARER, role);
    }

    @Test
    void deberiaConvertirStringARoleProtected() {
        Role role = Role.valueOf("PROTECTED");
        assertEquals(Role.PROTECTED, role);
    }

    @Test
    void deberiaConvertirStringARoleAdmin() {
        Role role = Role.valueOf("ADMIN");
        assertEquals(Role.ADMIN, role);
    }

    @Test
    void roleCarerDeberiaTenerNombreCorrecto() {
        assertEquals("CARER", Role.CARER.name());
    }

    @Test
    void roleProtectedDeberiaTenerNombreCorrecto() {
        assertEquals("PROTECTED", Role.PROTECTED.name());
    }

    @Test
    void roleAdminDeberiaTenerNombreCorrecto() {
        assertEquals("ADMIN", Role.ADMIN.name());
    }

    @Test
    void deberiaTenerTresRolesDefinidos() {
        assertEquals(3, Role.values().length);
    }

    @Test
    void noDeberiaAceptarValorInvalido() {
        assertThrows(IllegalArgumentException.class, () ->
                Role.valueOf("ROLE_INVALIDO"));
    }

    @Test
    void losRolesDeberianSerDistintosEntreSi() {
        assertNotEquals(Role.CARER, Role.PROTECTED);
        assertNotEquals(Role.CARER, Role.ADMIN);
        assertNotEquals(Role.PROTECTED, Role.ADMIN);
    }

    @Test
    void valuesDeberiaContenerTodosLosRoles() {
        Role[] roles = Role.values();

        assertTrue(java.util.Arrays.asList(roles).contains(Role.CARER));
        assertTrue(java.util.Arrays.asList(roles).contains(Role.PROTECTED));
        assertTrue(java.util.Arrays.asList(roles).contains(Role.ADMIN));
    }
}