package com.unlam.verabackend.domain.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class RelationshipTypeTest {

    @Test
    void deberiaRetornarFamilyMemberCuandoStringEsValido() {
        RelationshipType resultado =
                RelationshipType.fromString("FAMILY_MEMBER");

        assertEquals(RelationshipType.FAMILY_MEMBER, resultado);
    }

    @Test
    void deberiaRetornarTrustedContactCuandoStringEsValido() {
        RelationshipType resultado =
                RelationshipType.fromString("TRUSTED_CONTACT");

        assertEquals(RelationshipType.TRUSTED_CONTACT, resultado);
    }

    @Test
    void deberiaRetornarProfessionalCuandoStringEsValido() {
        RelationshipType resultado =
                RelationshipType.fromString("PROFESSIONAL");

        assertEquals(RelationshipType.PROFESSIONAL, resultado);
    }

    @Test
    void deberiaIgnorarMayusculasYMinusculas() {
        RelationshipType resultado =
                RelationshipType.fromString("family_member");

        assertEquals(RelationshipType.FAMILY_MEMBER, resultado);
    }

    @Test
    void deberiaIgnorarEspaciosAlInicioYFinal() {
        RelationshipType resultado =
                RelationshipType.fromString("  TRUSTED_CONTACT  ");

        assertEquals(RelationshipType.TRUSTED_CONTACT, resultado);
    }

    @Test
    void deberiaRetornarUndefinedCuandoEsNull() {
        RelationshipType resultado =
                RelationshipType.fromString(null);

        assertEquals(RelationshipType.UNDEFINED, resultado);
    }

    @Test
    void deberiaRetornarUndefinedCuandoValorEsDesconocido() {
        RelationshipType resultado =
                RelationshipType.fromString("OTRO");

        assertEquals(RelationshipType.UNDEFINED, resultado);
    }

    @Test
    void deberiaRetornarUndefinedCuandoStringEstaVacio() {
        RelationshipType resultado =
                RelationshipType.fromString("");

        assertEquals(RelationshipType.UNDEFINED, resultado);
    }

    @Test
    void deberiaRetornarDisplayNameCorrectoParaFamilyMember() {
        assertEquals(
                "Familiar",
                RelationshipType.FAMILY_MEMBER.getDisplayName()
        );
    }

    @Test
    void deberiaRetornarDisplayNameCorrectoParaTrustedContact() {
        assertEquals(
                "Contacto de Confianza",
                RelationshipType.TRUSTED_CONTACT.getDisplayName()
        );
    }

    @Test
    void deberiaRetornarDisplayNameCorrectoParaProfessional() {
        assertEquals(
                "Soporte Profesional",
                RelationshipType.PROFESSIONAL.getDisplayName()
        );
    }

    @Test
    void deberiaRetornarDisplayNameCorrectoParaUndefined() {
        assertEquals(
                "Sin Definir",
                RelationshipType.UNDEFINED.getDisplayName()
        );
    }
}