package com.unlam.verabackend.application.service;

import com.unlam.verabackend.domain.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PromptBuilderServiceTest {

    private PromptBuilderService promptBuilderService;

    @BeforeEach
    void setUp() {
        promptBuilderService = new PromptBuilderService();
    }

    // =========================================================================
    // Combinaciones de Inyección de Contenidos (rawText y fileText)
    // =========================================================================

    @Test
    void buildPrompt_WhenBothTextsAreNull_ShouldNotIncludeExposedContentSection() {
        // Arrange
        List<String> safeBrowsingReport = Collections.emptyList();
        Source source = Source.WEB;

        // Act
        String result = promptBuilderService.buildPrompt(safeBrowsingReport, null, null, source);

        // Assert
        assertNotNull(result);
        assertFalse(result.contains("### CONTENIDO EXPUESTO PARA ANALIZAR:"));
        assertFalse(result.contains("[Texto ingresado por el usuario]:"));
        assertFalse(result.contains("[Texto extraído del documento adjunto]:"));
    }

    @Test
    void buildPrompt_WhenBothTextsAreBlank_ShouldNotIncludeExposedContentSection() {
        // Arrange
        List<String> safeBrowsingReport = Collections.emptyList();
        String rawText = "   ";
        String fileText = "\n\t";
        Source source = Source.WEB;

        // Act
        String result = promptBuilderService.buildPrompt(safeBrowsingReport, rawText, fileText, source);

        // Assert
        assertFalse(result.contains("### CONTENIDO EXPUESTO PARA ANALIZAR:"));
    }

    @Test
    void buildPrompt_WhenOnlyRawTextHasContent_ShouldIncludeExposedContentAndOnlyRawTextSubSection() {
        // Arrange
        List<String> safeBrowsingReport = Collections.emptyList();
        String rawText = "Hola, ganaste un premio de ANSES";
        Source source = Source.WEB;

        // Act
        String result = promptBuilderService.buildPrompt(safeBrowsingReport, rawText, null, source);

        // Assert
        assertTrue(result.contains("### CONTENIDO EXPUESTO PARA ANALIZAR:"));
        assertTrue(result.contains("[Texto ingresado por el usuario]:\nHola, ganaste un premio de ANSES"));
        assertFalse(result.contains("[Texto extraído del documento adjunto]:"));
    }

    @Test
    void buildPrompt_WhenOnlyFileTextHasContent_ShouldIncludeExposedContentAndOnlyFileTextSubSection() {
        // Arrange
        List<String> safeBrowsingReport = Collections.emptyList();
        String rawText = "";
        String fileText = "Contenido extraido de un PDF sospechoso";
        Source source = Source.WEB;

        // Act
        String result = promptBuilderService.buildPrompt(safeBrowsingReport, rawText, fileText, source);

        // Assert
        assertTrue(result.contains("### CONTENIDO EXPUESTO PARA ANALIZAR:"));
        assertFalse(result.contains("[Texto ingresado por el usuario]:"));
        assertTrue(result.contains("[Texto extraído del documento adjunto]:\nContenido extraido de un PDF sospechoso"));
    }

    @Test
    void buildPrompt_WhenBothTextsHaveContent_ShouldIncludeBothSubSections() {
        // Arrange
        List<String> safeBrowsingReport = Collections.emptyList();
        String rawText = "Texto manual";
        String fileText = "Texto adjunto";
        Source source = Source.WEB;

        // Act
        String result = promptBuilderService.buildPrompt(safeBrowsingReport, rawText, fileText, source);

        // Assert
        assertTrue(result.contains("### CONTENIDO EXPUESTO PARA ANALIZAR:"));
        assertTrue(result.contains("[Texto ingresado por el usuario]:\nTexto manual"));
        assertTrue(result.contains("[Texto extraído del documento adjunto]:\nTexto adjunto"));
    }

    // =========================================================================
    // Reglas de Espacio según Origen (Source: MOBILE vs WEB)
    // =========================================================================

    @Test
    void buildPrompt_WhenSourceIsMobile_ShouldIncludeMobileRulesText() {
        // Arrange
        List<String> safeBrowsingReport = Collections.emptyList();
        Source source = Source.MOBILE;

        // Act
        String result = promptBuilderService.buildPrompt(safeBrowsingReport, null, null, source);

        // Assert
        assertTrue(result.contains("### REGLA DE ESPACIO (ORIGEN: MOBILE):"));
        assertTrue(result.contains("- El origen es un dispositivo MOBILE. Las respuestas para 'contentSummary'"));
        assertFalse(result.contains("- El origen es WEB. Podés EXPLAYARTE"));
    }

    @Test
    void buildPrompt_WhenSourceIsWeb_ShouldIncludeWebRulesText() {
        // Arrange
        List<String> safeBrowsingReport = Collections.emptyList();
        Source source = Source.WEB;

        // Act
        String result = promptBuilderService.buildPrompt(safeBrowsingReport, null, null, source);

        // Assert
        assertTrue(result.contains("### REGLA DE ESPACIO (ORIGEN: WEB):"));
        assertTrue(result.contains("- El origen es WEB. Podés EXPLAYARTE en detalle"));
        assertFalse(result.contains("- El origen es un dispositivo MOBILE. Las respuestas"));
    }

    // =========================================================================
    // Reporte Técnico e Impacto de Google Safe Browsing (Threats vs No Threats)
    // =========================================================================

    @Test
    void buildPrompt_WhenSafeBrowsingReportIsNull_ShouldTreatAsNoThreatsAndNotIncludeMandatoryHighRules() {
        // Arrange
        Source source = Source.WEB;

        // Act
        String result = promptBuilderService.buildPrompt(null, null, null, source);

        // Assert
        assertTrue(result.contains("- No se detectaron amenazas externas en los enlaces analizados por el servidor."));
        assertTrue(result.contains("* Si Safe Browsing está limpio, determiná vos el 'riskLevel'"));
        assertFalse(result.contains("REGLA DE NEGOCIO OBLIGATORIA: Como Google Safe Browsing detectó enlaces maliciosos"));
    }

    @Test
    void buildPrompt_WhenSafeBrowsingReportIsEmpty_ShouldTreatAsNoThreats() {
        // Arrange
        List<String> safeBrowsingReport = new ArrayList<>();
        Source source = Source.WEB;

        // Act
        String result = promptBuilderService.buildPrompt(safeBrowsingReport, null, null, source);

        // Assert
        assertTrue(result.contains("- No se detectaron amenazas externas en los enlaces analizados por el servidor."));
    }

    @Test
    void buildPrompt_WhenSafeBrowsingHasThreats_ShouldListThreatsAndIncludeMandatoryHighRules() {
        // Arrange
        List<String> safeBrowsingReport = List.of("http://url-maliciosa-uno.com", "http://phishing-banco.com");
        Source source = Source.WEB;

        // Act
        String result = promptBuilderService.buildPrompt(safeBrowsingReport, null, null, source);

        // Assert
        assertTrue(result.contains("- El análisis de enlaces arrojó novedades riesgosas y URLs peligrosas encontradas en el mensaje:"));
        assertTrue(result.contains("- http://url-maliciosa-uno.com"));
        assertTrue(result.contains("- http://phishing-banco.com"));

        assertTrue(result.contains("REGLA DE NEGOCIO OBLIGATORIA: Como Google Safe Browsing detectó enlaces maliciosos activos, el 'riskLevel' DEBE SER SÍ O SÍ 'HIGH'"));
        assertTrue(result.contains("REGLA DE NEGOCIO OBLIGATORIA: Si Google Safe Browsing reportó amenazas, el 'riskType' NO puede ser 'NONE'"));
        assertFalse(result.contains("* Si Safe Browsing está limpio, determiná vos el 'riskLevel'"));
    }

    // =========================================================================
    // Pruebas para buildChatSystemPrompt (Tipado con Enums Reales)
    // =========================================================================

    @Test
    void buildChatSystemPrompt_WhenAnalysisIsProvided_ShouldReturnAdultoMayorContextWithSource() {
        // Arrange
        Analysis analysis = Analysis.builder()
                .title("Email Sospechoso Netflix")
                .source(Source.WEB)
                .contentSummary("Contenido fraudulento emulando pasarela de pagos")
                .riskType(RiskType.PHISHING)
                .riskLevel(RiskLevel.HIGH)
                .riskPercentage(85)
                .suspiciousPatterns("Links extraños")
                .recommendation("No hacer clic")
                .build();

        // Act
        String result = promptBuilderService.buildChatSystemPrompt(analysis, null);

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("Te estás comunicando de forma directa con un ADULTO MAYOR."));
        assertTrue(result.contains("Extremadamente cálido, tierno, muy paciente y empático."));
        assertTrue(result.contains("Título del reporte: Email Sospechoso Netflix"));
        assertTrue(result.contains("Origen: WEB"));
        assertTrue(result.contains("Nivel de Riesgo Evaluado: HIGH (Porcentaje: 85%)"));
        assertFalse(result.contains("CONTACTO DE CONFIANZA"));
    }

    @Test
    void buildChatSystemPrompt_WhenAnalysisIsProvidedWithNullSource_ShouldFallbackToDesconocido() {
        // Arrange
        Analysis analysis = Analysis.builder()
                .title("Analisis Sin Origen")
                .source(null)
                .build();

        // Act
        String result = promptBuilderService.buildChatSystemPrompt(analysis, null);

        // Assert
        assertTrue(result.contains("Origen: Desconocido"));
    }

    @Test
    void buildChatSystemPrompt_WhenAlertIsProvided_ShouldReturnCuidadorContextWithSource() {
        // Arrange
        Alerts alert = Alerts.builder()
                .title("Acceso Indebido Detectado")
                .source(Source.MOBILE)
                .contentSummary("Intento de login repetido")
                .riskType(RiskType.IDENTITY_THEFT)
                .riskLevel(RiskLevel.HIGH)
                .riskPercentage(90)
                .suspiciousPatterns("IP sospechosa")
                .isResolved(false)
                .build();

        // Act
        String result = promptBuilderService.buildChatSystemPrompt(null, alert);

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("Te estás comunicando con el CONTACTO DE CONFIANZA"));
        assertTrue(result.contains("Profesional, directo, informativo, corporativo"));
        assertTrue(result.contains("Título de la alerta: Acceso Indebido Detectado"));
        assertTrue(result.contains("Vía de entrada: MOBILE"));
        assertTrue(result.contains("Estado de la Alerta: ACTIVA - REQUIERE ACCIÓN"));
        assertFalse(result.contains("ADULTO MAYOR"));
    }

    @Test
    void buildChatSystemPrompt_WhenAlertIsProvidedWithNullSourceAndResolvedTrue_ShouldCoverInternalBranches() {
        // Arrange
        Alerts alert = Alerts.builder()
                .source(null)
                .isResolved(true)
                .build();

        // Act
        String result = promptBuilderService.buildChatSystemPrompt(null, alert);

        // Assert
        assertTrue(result.contains("Vía de entrada: Desconocido"));
        assertTrue(result.contains("Estado de la Alerta: Resuelta"));
    }

    @Test
    void buildChatSystemPrompt_WhenBothAnalysisAndAlertAreNull_ShouldReturnNeutroContext() {
        // Act
        String result = promptBuilderService.buildChatSystemPrompt(null, null);

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("PÚBLICO: Usuario general de la aplicación."));
        assertTrue(result.contains("TONO OBLIGATORIO: Equilibrado, cordial, profesional, pedagógico y empático."));
        assertTrue(result.contains("### REGLA FINAL DE RESPUESTA (ESTILO CHAT HUMANO):"));
        assertFalse(result.contains("### CONTEXTO DEL ANÁLISIS PREVIO REALIZADO:"));
    }
}