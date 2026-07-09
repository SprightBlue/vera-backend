package com.unlam.verabackend.application.service;

import com.unlam.verabackend.domain.model.Analysis;
import com.unlam.verabackend.domain.model.RiskType;
import com.unlam.verabackend.domain.model.Source;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class PromptBuilderServiceTest {

    private PromptBuilderService promptBuilderService;

    @BeforeEach
    void setUp() {
        promptBuilderService = new PromptBuilderService();
    }

    @Test
    void deberiaConstruirPromptParaMobileYConTextoSinAmenazas() {
        String rawText = "Mensaje sospechoso de WhatsApp";
        String fileText = null; 
        Source source = Source.MOBILE;
        List<String> safeBrowsingReport = List.of(); 

        String prompt = promptBuilderService.buildPrompt(safeBrowsingReport, rawText, fileText, source);

        assertNotNull(prompt);
        assertTrue(prompt.contains("El origen es MOBILE"));
        assertTrue(prompt.contains("[Texto]: " + rawText));
        assertTrue(prompt.contains("Las listas negras automatizadas no detectaron registros"));
    }

    @Test
    void deberiaConstruirPromptParaWebYConArchivoConAmenazas() {

        String rawText = ""; 
        String fileText = "Contenido de un PDF escaneado";
        Source source = Source.WEB;
        List<String> safeBrowsingReport = List.of("Malware detectado", "Phishing en URL"); // Con amenazas

        String prompt = promptBuilderService.buildPrompt(safeBrowsingReport, rawText, fileText, source);

        assertNotNull(prompt);

        assertTrue(prompt.contains("El origen es WEB"));
        assertTrue(prompt.contains("[Documento]: " + fileText));
        assertTrue(prompt.contains("Amenazas críticas reportadas directamente por el servidor:"));
        assertTrue(prompt.contains("- Malware detectado"));
        assertTrue(prompt.contains("Al existir una amenaza explícita en las listas negras"));
    }

    @Test
    void deberiaConstruirPromptSinTextosParaAnalizar() {
        String prompt = promptBuilderService.buildPrompt(null, null, null, Source.WEB);

        assertNotNull(prompt);
        assertFalse(prompt.contains("### CONTENIDO A ANALIZAR:"));
    }

    @Test
    void deberiaConstruirPromptDeChatSinAnalisisPrevio() {
  
        String prompt = promptBuilderService.buildChatSystemPrompt(null);

        assertNotNull(prompt);
        assertTrue(prompt.contains("REGLAS DE CHAT:"));
        assertFalse(prompt.contains("### CONTEXTO DEL ANÁLISIS PREVIO PARA VERA:"));
    }

    @Test
    void deberiaConstruirPromptDeChatConAnalisisPrevio() {
      
        Analysis analysisMock = mock(Analysis.class);
        when(analysisMock.getRiskType()).thenReturn(RiskType.SUSPICIOUS_COMMUNICATION); 
        when(analysisMock.getSuspiciousPatterns()).thenReturn("Pide transferencia urgente");
        when(analysisMock.getContentSummary()).thenReturn("Intento de estafa bancaria");

        String prompt = promptBuilderService.buildChatSystemPrompt(analysisMock);

        assertNotNull(prompt);
        assertTrue(prompt.contains("### CONTEXTO DEL ANÁLISIS PREVIO PARA VERA:"));
        assertTrue(prompt.contains("SUSPICIOUS_COMMUNICATION"));
        assertTrue(prompt.contains("Pide transferencia urgente"));
        assertTrue(prompt.contains("Intento de estafa bancaria"));
    }

    @Test
    void deberiaGenerarPromptParaTituloCorrectamente() {
        String primerMensaje = "Hola, me llegó este correo raro";

        String prompt = promptBuilderService.buildTitleGenerationPrompt(primerMensaje);

        assertNotNull(prompt);
        assertTrue(prompt.contains("Generá un título conciso"));
        assertTrue(prompt.endsWith(primerMensaje));
    }
}