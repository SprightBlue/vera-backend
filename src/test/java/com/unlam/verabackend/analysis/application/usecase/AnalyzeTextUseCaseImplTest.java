package com.unlam.verabackend.analysis.application.usecase;

import com.unlam.verabackend.analysis.application.service.AnalysisBusinessRulesService;
import com.unlam.verabackend.analysis.domain.model.Analysis;
import com.unlam.verabackend.analysis.domain.model.Message;
import com.unlam.verabackend.analysis.domain.model.MessageSource;
import com.unlam.verabackend.analysis.domain.model.RiskLevel;
import com.unlam.verabackend.analysis.domain.ports.out.AnalysisRepositoryPort;
import com.unlam.verabackend.analysis.domain.ports.out.GeminiAnalysisPort;
import com.unlam.verabackend.analysis.domain.ports.out.MessageRepositoryPort;
import com.unlam.verabackend.analysis.domain.ports.out.SafeBrowsingPort;
import com.unlam.verabackend.analysis.infrastructure.dto.GeminiAnalysisResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AnalyzeTextUseCaseImplTest {

    @Mock
    private MessageRepositoryPort messageRepositoryPort;
    @Mock
    private AnalysisRepositoryPort analysisRepositoryPort;
    @Mock
    private SafeBrowsingPort safeBrowsingPort;
    @Mock
    private GeminiAnalysisPort geminiAnalysisPort;

    private AnalyzeTextUseCaseImpl useCase;

    @BeforeEach
    void setUp() {
        useCase = new AnalyzeTextUseCaseImpl(
                messageRepositoryPort,
                analysisRepositoryPort,
                safeBrowsingPort,
                geminiAnalysisPort,
                new AnalysisBusinessRulesService()
        );
    }

    @Test
    void shouldReturnLowRiskWhenGeminiReturnsLowAndMessageHasNoUrl() {
        Message message = buildMessage("Hola, queria confirmar el horario de la clase.");
        when(geminiAnalysisPort.analyzeMessage(any())).thenReturn(
                new GeminiAnalysisResponse(false, RiskLevel.LOW, "Ninguno", "Continuar con normalidad")
        );

        Analysis result = useCase.analyzeMessage(message);

        assertEquals(RiskLevel.LOW, result.getRiskLevel());
        verify(safeBrowsingPort, never()).checkUrl(any());
        verify(messageRepositoryPort).save(eq(message));
        verify(analysisRepositoryPort).save(any(Analysis.class));
    }

    @Test
    void shouldReturnMediumRiskWhenGeminiReturnsMediumAndMessageHasNoUrl() {
        Message message = buildMessage("Te escribo urgente, por favor responde apenas puedas.");
        when(geminiAnalysisPort.analyzeMessage(any())).thenReturn(
                new GeminiAnalysisResponse(true, RiskLevel.MEDIUM, "Urgencia", "Verificar con una fuente oficial")
        );

        Analysis result = useCase.analyzeMessage(message);

        assertEquals(RiskLevel.MEDIUM, result.getRiskLevel());
        verify(safeBrowsingPort, never()).checkUrl(any());
    }

    @Test
    void shouldReturnHighRiskWhenGeminiReturnsHighAndMessageHasNoUrl() {
        Message message = buildMessage("Tu cuenta esta comprometida, envia tu clave ahora.");
        when(geminiAnalysisPort.analyzeMessage(any())).thenReturn(
                new GeminiAnalysisResponse(true, RiskLevel.HIGH, "Phishing", "No responder y reportar")
        );

        Analysis result = useCase.analyzeMessage(message);

        assertEquals(RiskLevel.HIGH, result.getRiskLevel());
        assertTrue(result.isThreat());
    }

    @Test
    void shouldForceHighRiskAndThreatWhenUrlIsMalicious() {
        Message message = buildMessage("Mira este link: https://sitio-raro.com/premio");
        when(safeBrowsingPort.checkUrl("https://sitio-raro.com/premio"))
                .thenReturn(true);
        when(geminiAnalysisPort.analyzeMessage(any())).thenReturn(
                new GeminiAnalysisResponse(false, RiskLevel.LOW, "", "")
        );

        Analysis result = useCase.analyzeMessage(message);

        assertEquals(RiskLevel.HIGH, result.getRiskLevel());
        assertTrue(result.isThreat());
        verify(safeBrowsingPort).checkUrl("https://sitio-raro.com/premio");
    }

    @Test
    void shouldUseGeminiDecisionWhenUrlIsNotMalicious() {
        Message message = buildMessage("Mira este link: https://sitio-confiable.com/info");
        when(safeBrowsingPort.checkUrl("https://sitio-confiable.com/info"))
                .thenReturn(false);
        when(geminiAnalysisPort.analyzeMessage(any())).thenReturn(
                new GeminiAnalysisResponse(false, RiskLevel.LOW, "Sin patrones", "Sin accion")
        );

        Analysis result = useCase.analyzeMessage(message);

        assertEquals(RiskLevel.LOW, result.getRiskLevel());
        assertFalse(result.isThreat());
        verify(safeBrowsingPort).checkUrl("https://sitio-confiable.com/info");
    }

    @Test
    void shouldThrowExceptionWhenMessageIsEmpty() {
        Message message = buildMessage("   ");

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> useCase.analyzeMessage(message));

        assertEquals("El mensaje no puede estar vacio", exception.getMessage());
        verify(messageRepositoryPort, never()).save(any());
        verify(analysisRepositoryPort, never()).save(any());
    }

    @Test
    void shouldDefaultSourceToUnknownWhenSourceIsNotSpecified() {
        Message message = new Message(
                UUID.randomUUID(),
                1L,
                "Mensaje sin plataforma especificada",
                null,
                LocalDateTime.now()
        );
        when(geminiAnalysisPort.analyzeMessage(any())).thenReturn(
                new GeminiAnalysisResponse(false, RiskLevel.LOW, "Ninguno", "Continuar con normalidad")
        );

        useCase.analyzeMessage(message);

        assertEquals(MessageSource.UNKNOWN, message.getSource());
        verify(messageRepositoryPort).save(eq(message));
    }

    private Message buildMessage(String content) {
        return new Message(
                UUID.randomUUID(),
                1L,
                content,
                MessageSource.WHATSAPP,
                LocalDateTime.now()
        );
    }
}

