package com.unlam.verabackend.domain.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class RiskLevelTest {

    @Test
    void deberiaRetornarLowCuandoStringEsValido() {
        RiskLevel resultado =
                RiskLevel.fromString("LOW");

        assertEquals(RiskLevel.LOW, resultado);
    }

    @Test
    void deberiaRetornarMediumCuandoStringEsValido() {
        RiskLevel resultado =
                RiskLevel.fromString("MEDIUM");

        assertEquals(RiskLevel.MEDIUM, resultado);
    }

    @Test
    void deberiaRetornarHighCuandoStringEsValido() {
        RiskLevel resultado =
                RiskLevel.fromString("HIGH");

        assertEquals(RiskLevel.HIGH, resultado);
    }

    @Test
    void deberiaIgnorarMayusculasYMinusculas() {
        RiskLevel resultado =
                RiskLevel.fromString("high");

        assertEquals(RiskLevel.HIGH, resultado);
    }

    @Test
    void deberiaIgnorarEspaciosAlInicioYFinal() {
        RiskLevel resultado =
                RiskLevel.fromString("  MEDIUM  ");

        assertEquals(RiskLevel.MEDIUM, resultado);
    }

    @Test
    void deberiaRetornarUndefinedCuandoEsNull() {
        RiskLevel resultado =
                RiskLevel.fromString(null);

        assertEquals(RiskLevel.UNDEFINED, resultado);
    }

    @Test
    void deberiaRetornarUndefinedCuandoValorEsDesconocido() {
        RiskLevel resultado =
                RiskLevel.fromString("OTRO");

        assertEquals(RiskLevel.UNDEFINED, resultado);
    }

    @Test
    void deberiaRetornarUndefinedCuandoStringEstaVacio() {
        RiskLevel resultado =
                RiskLevel.fromString("");

        assertEquals(RiskLevel.UNDEFINED, resultado);
    }

    @Test
    void deberiaRetornarDisplayNameCorrectoParaLow() {
        assertEquals(
                "Bajo",
                RiskLevel.LOW.getDisplayName()
        );
    }

    @Test
    void deberiaRetornarDisplayNameCorrectoParaMedium() {
        assertEquals(
                "Medio",
                RiskLevel.MEDIUM.getDisplayName()
        );
    }

    @Test
    void deberiaRetornarDisplayNameCorrectoParaHigh() {
        assertEquals(
                "Alto",
                RiskLevel.HIGH.getDisplayName()
        );
    }

    @Test
    void deberiaRetornarDisplayNameCorrectoParaUndefined() {
        assertEquals(
                "Sin Definir",
                RiskLevel.UNDEFINED.getDisplayName()
        );
    }
}