package com.unlam.verabackend.application.service;

import com.unlam.verabackend.domain.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PromptBuilderServiceTest {

    private PromptBuilderService promptBuilderService;

    @BeforeEach
    void setUp() {
        promptBuilderService = new PromptBuilderService();
    }

    @Test
    void buildPrompt_WhenBothTextsAreNull_ShouldNotIncludeExposedContentSection() {
        String result = promptBuilderService.buildPrompt(Collections.emptyList(), null, null, Source.WEB);
        assertFalse(result.contains("### CONTENIDO EXPUESTO PARA ANALIZAR:"));
    }

    @Test
    void buildPrompt_WhenOnlyRawTextHasContent_ShouldIncludeExposedContent() {
        String result = promptBuilderService.buildPrompt(Collections.emptyList(), "Premio", null, Source.WEB);
        assertTrue(result.contains("[Texto ingresado por el usuario]:\nPremio"));
    }

    @Test
    void buildPrompt_WhenSourceIsMobile_ShouldIncludeMobileRules() {
        String result = promptBuilderService.buildPrompt(Collections.emptyList(), null, null, Source.MOBILE);
        assertTrue(result.contains("### REGLA DE ESPACIO (ORIGEN: MOBILE):"));
    }

    @Test
    void buildPrompt_WhenSafeBrowsingHasThreats_ShouldIncludeMandatoryHighRules() {
        String result = promptBuilderService.buildPrompt(List.of("http://phishing.com"), null, null, Source.WEB);
        assertTrue(result.contains("REGLA DE NEGOCIO OBLIGATORIA"));
    }

    @Test
    void buildChatSystemPrompt_WhenAlertIsProvided_ShouldReturnPedagogicToneForCuidador() {
        // Arrange
        Alerts alert = Alerts.builder()
                .title("Alerta Login")
                .source(Source.MOBILE)
                .contentSummary("Login repetido")
                .riskType(RiskType.IDENTITY_THEFT)
                .riskLevel(RiskLevel.HIGH)
                .riskPercentage(90)
                .build();

        // Act
        String result = promptBuilderService.buildChatSystemPrompt(null, alert);

        // Assert
        assertTrue(result.contains("TONO OBLIGATORIO: Sencillo, directo, extremadamente claro, paciente y empático."));
        assertTrue(result.contains("Evitá tecnicismos informáticos puros e innecesarios."));
        assertTrue(result.contains("explicalos de forma didáctica con metáforas simples"));
    }

    @Test
    void buildTitleGenerationPrompt_ShouldReturnCorrectInstructions() {
        // Arrange
        String userMessage = "Hola, me llegó un SMS raro del banco";

        // Act
        String result = promptBuilderService.buildTitleGenerationPrompt(userMessage);

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("Sos un módulo automatizado de VERA"));
        assertTrue(result.contains("- Extensión máxima: 4 o 5 palabras."));
        assertTrue(result.contains("Hola, me llegó un SMS raro del banco"));
    }

    @Test
    void buildChatSystemPrompt_WhenAnalysisIsProvided_ShouldReturnAdultoMayorContext() {
        Analysis analysis = Analysis.builder()
                .title("Premio Falso")
                .source(Source.WEB)
                .riskType(RiskType.PHISHING)
                .riskLevel(RiskLevel.HIGH)
                .riskPercentage(80)
                .build();

        String result = promptBuilderService.buildChatSystemPrompt(analysis, null);

        assertTrue(result.contains("Te estás comunicando de forma directa con un ADULTO MAYOR."));
        assertTrue(result.contains("Título del reporte: Premio Falso"));
    }

    @Test
    void buildChatSystemPrompt_WhenNothingProvided_ShouldReturnGeneralContext() {
        String result = promptBuilderService.buildChatSystemPrompt(null, null);
        assertTrue(result.contains("PÚBLICO: Usuario general de la aplicación."));
    }
}