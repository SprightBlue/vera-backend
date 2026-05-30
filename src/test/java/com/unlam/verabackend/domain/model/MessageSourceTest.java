package com.unlam.verabackend.domain.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class MessageSourceTest {

    @Test
    void deberiaRetornarWhatsappCuandoStringEsValido() {
        MessageSource resultado =
                MessageSource.fromString("WHATSAPP");

        assertEquals(MessageSource.WHATSAPP, resultado);
    }

    @Test
    void deberiaRetornarTelegramCuandoStringEsValido() {
        MessageSource resultado =
                MessageSource.fromString("TELEGRAM");

        assertEquals(MessageSource.TELEGRAM, resultado);
    }

    @Test
    void deberiaIgnorarMayusculasYMinusculas() {
        MessageSource resultado =
                MessageSource.fromString("whatsapp");

        assertEquals(MessageSource.WHATSAPP, resultado);
    }

    @Test
    void deberiaIgnorarEspaciosAlInicioYFinal() {
        MessageSource resultado =
                MessageSource.fromString("  TELEGRAM  ");

        assertEquals(MessageSource.TELEGRAM, resultado);
    }

    @Test
    void deberiaRetornarUnknownCuandoEsNull() {
        MessageSource resultado =
                MessageSource.fromString(null);

        assertEquals(MessageSource.UNKNOWN, resultado);
    }

    @Test
    void deberiaRetornarUnknownCuandoValorEsDesconocido() {
        MessageSource resultado =
                MessageSource.fromString("DISCORD");

        assertEquals(MessageSource.UNKNOWN, resultado);
    }

    @Test
    void deberiaRetornarUnknownCuandoStringEstaVacio() {
        MessageSource resultado =
                MessageSource.fromString("");

        assertEquals(MessageSource.UNKNOWN, resultado);
    }

    @Test
    void deberiaRetornarDisplayNameCorrectoParaWhatsapp() {
        assertEquals(
                "WhatsApp",
                MessageSource.WHATSAPP.getDisplayName()
        );
    }

    @Test
    void deberiaRetornarDisplayNameCorrectoParaTelegram() {
        assertEquals(
                "Telegram",
                MessageSource.TELEGRAM.getDisplayName()
        );
    }

    @Test
    void deberiaRetornarDisplayNameCorrectoParaUnknown() {
        assertEquals(
                "Origen Desconocido",
                MessageSource.UNKNOWN.getDisplayName()
        );
    }

    @Test
    void deberiaTenerTresSourcesDefinidos() {
        assertEquals(3, MessageSource.values().length);
    }

    @Test
    void whatsappYTelegramDeberianSerDistintos() {
        assertNotEquals(
                MessageSource.WHATSAPP,
                MessageSource.TELEGRAM
        );
    }
}