package com.unlam.verabackend.analysis.application.usecase;

import com.unlam.verabackend.analysis.application.service.AnalysisService;
import com.unlam.verabackend.analysis.domain.model.*;
import com.unlam.verabackend.analysis.domain.ports.out.*;
import com.unlam.verabackend.analysis.infrastructure.dto.GeminiDto;
import com.unlam.verabackend.analysis.infrastructure.dto.SafeBrowsingDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AnalyzeMessageUseCaseImplTest {

    @Mock private MessageRepositoryPort messageRepositoryPort;
    @Mock private AnalysisRepositoryPort analysisRepositoryPort;
    @Mock private SafeBrowsingApiPort safeBrowsingApiPort;
    @Mock private GeminiApiPort geminiApiPort;
    @Mock private UserCaregiverRepositoryPort userCaregiverRepositoryPort;
    @Mock private RiskAlertRepositoryPort riskAlertRepositoryPort;

    @Spy private AnalysisService analysisService;

    private AnalyzeMessageUseCaseImpl useCase;

    @BeforeEach
    void setUp() {
        useCase = new AnalyzeMessageUseCaseImpl(
                messageRepositoryPort,
                analysisRepositoryPort,
                safeBrowsingApiPort,
                geminiApiPort,
                analysisService,
                userCaregiverRepositoryPort,
                riskAlertRepositoryPort
        );
    }

    @Test
    @DisplayName("Debe lanzar IllegalArgumentException si el objeto Message recibido es nulo")
    void analyzeMessage_WhenMessageIsNull_ShouldThrowException() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> useCase.analyzeMessage(null));

        assertEquals("El mensaje a analizar no puede ser nulo", exception.getMessage());
        verifyNoInteractions(messageRepositoryPort, analysisRepositoryPort, safeBrowsingApiPort, geminiApiPort);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "\n", "\t"})
    @DisplayName("Debe lanzar IllegalArgumentException si el contenido del mensaje está en blanco o vacío")
    void analyzeMessage_WhenContentIsBlank_ShouldThrowException(String blankContent) {
        Message message = new Message(UUID.randomUUID(), 10L, blankContent, MessageSource.WHATSAPP, LocalDateTime.now());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> useCase.analyzeMessage(message));

        assertEquals("El mensaje a analizar no puede ser nulo", exception.getMessage());
        verifyNoInteractions(messageRepositoryPort, analysisRepositoryPort, safeBrowsingApiPort, geminiApiPort);
    }

    @ParameterizedTest
    @MethodSource("provideUrlAndSemanticScenarios")
    @DisplayName("Debe orquestar el flujo y disparar alertas según el nivel de riesgo analizado")
    void analyzeMessage_WithVariousScenarios_ShouldBehaveCorrectly(
            String messageContent,
            SafeBrowsingDto mockSafeBrowsingResponse,
            GeminiDto mockGeminiResponse,
            RiskLevel expectedRiskLevel,
            int expectedAlertsCount
    ) {
        Message message = new Message(UUID.randomUUID(), 50L, messageContent, MessageSource.WHATSAPP, LocalDateTime.now());

        when(safeBrowsingApiPort.checkUrls(any())).thenReturn(mockSafeBrowsingResponse);
        when(geminiApiPort.analyzeMessage(anyString())).thenReturn(mockGeminiResponse);

        if (expectedRiskLevel == RiskLevel.HIGH) {
            UserCaregiver caregiver = new UserCaregiver(1L, 50L, 999L, RelationshipType.FAMILY_MEMBER, "112233", "caregiver@mail.com", LocalDateTime.now());
            when(userCaregiverRepositoryPort.findByUserId(50L)).thenReturn(List.of(caregiver));
        }

        Analysis result = useCase.analyzeMessage(message);

        assertNotNull(result);
        assertEquals(expectedRiskLevel, result.getRiskLevel());

        verify(messageRepositoryPort, times(1)).save(message);
        verify(analysisRepositoryPort, times(1)).save(result);

        verify(riskAlertRepositoryPort, times(expectedAlertsCount)).save(any(RiskAlert.class));
    }

    private static Stream<Arguments> provideUrlAndSemanticScenarios() {
        return Stream.of(
                Arguments.of(
                        "Hola ma, avísame cuando llegues a casa.",
                        SafeBrowsingDto.empty(),
                        new GeminiDto("LOW", "Mensaje familiar totalmente cotidiano", "No requiere ninguna acción"),
                        RiskLevel.LOW,
                        0
                ),

                Arguments.of(
                        "Te paso la receta de la torta que me pediste: https://recetas-de-cocina.com/torta",
                        SafeBrowsingDto.empty(),
                        new GeminiDto("LOW", "Enlace legítimo a un portal gastronómico común", "Podes navegar el sitio de recetas"),
                        RiskLevel.LOW,
                        0
                ),

                Arguments.of(
                        "SU CUENTA HA SIDO SUSPENDIDA. Ingrese aquí para reactivar sus credenciales: http://banco-fraude-alerta.com",
                        new SafeBrowsingDto(true, 1, List.of("SOCIAL_ENGINEERING"), List.of("http://banco-fraude-alerta.com")),
                        new GeminiDto("HIGH", "Phishing directo detectado por base de datos de amenazas globales", "Elimina el mensaje inmediatamente"),
                        RiskLevel.HIGH,
                        1
                ),

                Arguments.of(
                        "Hola abuela, soy tu nieto más grande. Cambié el número porque se me rompió el otro. Necesito que vayas al banco a sacar todos los dólares porque va a haber un corralito financiero y pasa un amigo mío contador a buscarlos por tu casa en media hora.",
                        SafeBrowsingDto.empty(),
                        new GeminiDto("HIGH", "Detección semántica de estafa por manipulación psicológica y urgencia de activos", "No entregues dinero ni valores a nadie. Cortá la comunicación y llamá a tu nieto directamente a su número de teléfono habitual."),
                        RiskLevel.HIGH,
                        1
                )
        );
    }
}
