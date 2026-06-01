package com.unlam.verabackend.domain.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class AlertResponseTest {

    @Test
    void deberiaCrearAlertResponseCorrectamente() {
        AlertResponse response =
                new AlertResponse(
                        1L,
                        "Alerta detectada",
                        "HIGH"
                );

        assertNotNull(response);
    }

    @Test
    void deberiaGuardarIdCorrectamente() {
        AlertResponse response =
                new AlertResponse(
                        1L,
                        "Alerta detectada",
                        "HIGH"
                );

        assertEquals(1L, response.getId());
    }

    @Test
    void deberiaGuardarTituloCorrectamente() {
        AlertResponse response =
                new AlertResponse(
                        1L,
                        "Alerta detectada",
                        "HIGH"
                );

        assertEquals(
                "Alerta detectada",
                response.getTitle()
        );
    }

    @Test
    void deberiaGuardarRiskCorrectamente() {
        AlertResponse response =
                new AlertResponse(
                        1L,
                        "Alerta detectada",
                        "HIGH"
                );

        assertEquals(
                "HIGH",
                response.getRisk()
        );
    }

    @Test
    void idNoDeberiaSerNuloCuandoSeAsigna() {
        AlertResponse response =
                new AlertResponse(
                        5L,
                        "Test",
                        "LOW"
                );

        assertNotNull(response.getId());
    }

    @Test
    void tituloNoDeberiaSerNuloCuandoSeAsigna() {
        AlertResponse response =
                new AlertResponse(
                        5L,
                        "Titulo",
                        "LOW"
                );

        assertNotNull(response.getTitle());
    }

    @Test
    void riskNoDeberiaSerNuloCuandoSeAsigna() {
        AlertResponse response =
                new AlertResponse(
                        5L,
                        "Titulo",
                        "MEDIUM"
                );

        assertNotNull(response.getRisk());
    }

    @Test
    void deberiaAceptarRiskHigh() {
        AlertResponse response =
                new AlertResponse(
                        1L,
                        "Peligro",
                        "HIGH"
                );

        assertEquals("HIGH", response.getRisk());
    }

    @Test
    void deberiaAceptarRiskLow() {
        AlertResponse response =
                new AlertResponse(
                        1L,
                        "Seguro",
                        "LOW"
                );

        assertEquals("LOW", response.getRisk());
    }

    @Test
    void tituloNoDeberiaEstarVacio() {
        AlertResponse response =
                new AlertResponse(
                        1L,
                        "Mensaje sospechoso",
                        "MEDIUM"
                );

        assertFalse(
                response.getTitle().isEmpty()
        );
    }
}