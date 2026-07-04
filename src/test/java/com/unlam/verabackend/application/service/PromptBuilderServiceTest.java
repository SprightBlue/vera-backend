package com.unlam.verabackend.application.service;

import com.unlam.verabackend.domain.model.Analysis;
import com.unlam.verabackend.domain.model.Source;
import com.unlam.verabackend.domain.model.RiskType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class PromptBuilderServiceTest {

    @InjectMocks
    private PromptBuilderService promptBuilderService;

    @Test
    @DisplayName("Debe omitir la sección de contenido a analizar cuando el texto base y el del archivo son nulos o vacíos")
    void buildPrompt_WhenBothTextsAreNullOrBlank_ShouldNotIncludeContentSection() {
        String result = promptBuilderService.buildPrompt(Collections.emptyList(), null, "   ", Source.WEB);
        assertFalse(result.contains("### CONTENIDO A ANALIZAR:"));
    }

    @Test
    @DisplayName("Debe incluir la sección de contenido a analizar cuando se proporciona texto plano")
    void buildPrompt_WhenRawTextHasContent_ShouldIncludeTextInContentSection() {
        String result = promptBuilderService.buildPrompt(Collections.emptyList(), "Premio ganado", null, Source.WEB);

        assertTrue(result.contains("### CONTENIDO A ANALIZAR:"));
        assertTrue(result.contains("[Texto]: Premio ganado"));
    }

    @Test
    @DisplayName("Debe incluir la sección de contenido a analizar cuando se proporciona texto de un archivo")
    void buildPrompt_WhenFileTextHasContent_ShouldIncludeDocumentInContentSection() {
        String result = promptBuilderService.buildPrompt(Collections.emptyList(), null, "Contenido del PDF", Source.WEB);

        assertTrue(result.contains("### CONTENIDO A ANALIZAR:"));
        assertTrue(result.contains("[Documento]: Contenido del PDF"));
    }

    @Test
    @DisplayName("Debe aplicar las reglas restrictivas de espacio cortas si el origen de la petición es MOBILE")
    void buildPrompt_WhenSourceIsMobile_ShouldIncludeMobileRules() {
        String result = promptBuilderService.buildPrompt(Collections.emptyList(), null, null, Source.MOBILE);

        assertTrue(result.contains("### REGLA DE ESPACIO (ORIGEN: MOBILE):"));
        assertTrue(result.contains("DEBEN SER MUY ACOTADAS (un enunciado breve por campo)"));
    }

    @Test
    @DisplayName("Debe aplicar las reglas de espacio compactas para tarjetas si el origen de la petición es WEB")
    void buildPrompt_WhenSourceIsWeb_ShouldIncludeWebRules() {
        String result = promptBuilderService.buildPrompt(Collections.emptyList(), null, null, Source.WEB);

        assertTrue(result.contains("### REGLA DE ESPACIO (ORIGEN: WEB):"));
        assertTrue(result.contains("Limitá el texto a un único párrafo corto"));
        assertTrue(result.contains("dentro de una tarjeta (Card)"));
    }

    @Test
    @DisplayName("Debe indicar en el prompt que las listas automatizadas están limpias si Safe Browsing no reporta amenazas")
    void buildPrompt_WhenSafeBrowsingIsEmpty_ShouldIncludeCleanReportRules() {
        String result = promptBuilderService.buildPrompt(Collections.emptyList(), null, null, Source.WEB);

        assertTrue(result.contains("Las listas negras automatizadas no detectaron registros de amenazas"));
        assertTrue(result.contains("Asigná 'LOW' por defecto únicamente si el contenido es verificado"));
    }

    @Test
    @DisplayName("Debe forzar la regla de veredicto crítico de nivel HIGH si Safe Browsing detecta amenazas")
    void buildPrompt_WhenSafeBrowsingHasThreats_ShouldIncludeMandatoryHighRules() {
        List<String> threats = List.of("MALWARE", "SOCIAL_ENGINEERING");
        String result = promptBuilderService.buildPrompt(threats, null, null, Source.WEB);

        assertTrue(result.contains("Amenazas críticas reportadas directamente por el servidor:"));
        assertTrue(result.contains("- MALWARE"));
        assertTrue(result.contains("- SOCIAL_ENGINEERING"));
        assertTrue(result.contains("el veredicto obligatoriamente es crítico: riskLevel=HIGH"));
    }

    @Test
    @DisplayName("Debe armar las directrices base del sistema para el chat omitiendo el contexto si el objeto Analysis es nulo")
    void buildChatSystemPrompt_WhenAnalysisIsNull_ShouldReturnGeneralGuidelinesOnly() {
        String result = promptBuilderService.buildChatSystemPrompt(null);

        assertTrue(result.contains("Sos VERA, un sistema experto en ciberseguridad"));
        assertTrue(result.contains("Actuá como un médico de cabecera familiar"));
        assertTrue(result.contains("REGLAS DE CHAT:"));
        assertFalse(result.contains("### CONTEXTO DEL ANÁLISIS PREVIO PARA VERA:"));
    }

    @Test
    @DisplayName("Debe omitir el bloque de contexto si el objeto Analysis no es nulo pero sus propiedades de negocio internas son nulas")
    void buildChatSystemPrompt_WhenAnalysisPropertiesAreNull_ShouldOmitContext() {
        Analysis blankAnalysis = Analysis.builder().id(UUID.randomUUID()).riskType(null).build();

        String result = promptBuilderService.buildChatSystemPrompt(blankAnalysis);

        assertTrue(result.contains("Sos VERA, un sistema experto en ciberseguridad"));
        assertFalse(result.contains("### CONTEXTO DEL ANÁLISIS PREVIO PARA VERA:"));
    }

    @Test
    @DisplayName("Debe inyectar el contexto analítico completo e incluir las reglas de respuestas fluidas de 1 o 2 oraciones")
    void buildChatSystemPrompt_WhenAnalysisIsProvided_ShouldReturnChatPromptWithAnalysisContext() {
        Analysis analysis = Analysis.builder()
                .riskType(RiskType.TRANSFERRED_MONEY)
                .suspiciousPatterns("Pedido de transferencia inmediata")
                .contentSummary("El atacante finge ser el hijo de la víctima")
                .build();

        String result = promptBuilderService.buildChatSystemPrompt(analysis);

        assertTrue(result.contains("### CONTEXTO DEL ANÁLISIS PREVIO PARA VERA:"));
        assertTrue(result.contains("- Tipo de Riesgo Detectado: TRANSFERRED_MONEY"));
        assertTrue(result.contains("- Patrones sospechosos: Pedido de transferencia inmediata"));
        assertTrue(result.contains("- Resumen de la situación: El atacante finge ser el hijo de la víctima"));
        assertTrue(result.contains("Limitá la longitud de tu respuesta a un máximo de 1 o 2 oraciones"));
    }

    @Test
    @DisplayName("Debe retornar las instrucciones de formateo de títulos concatenando exactamente el primer mensaje del usuario")
    void buildTitleGenerationPrompt_WhenMessageIsProvided_ShouldReturnCorrectInstructions() {
        String userMessage = "Hola, me llegó un SMS raro del banco";

        String result = promptBuilderService.buildTitleGenerationPrompt(userMessage);

        assertNotNull(result);
        assertTrue(result.contains("Generá un título conciso (máximo 5 palabras)"));
        assertTrue(result.endsWith("Hola, me llegó un SMS raro del banco"));
    }
}