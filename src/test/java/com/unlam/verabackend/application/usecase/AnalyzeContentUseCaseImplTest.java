package com.unlam.verabackend.application.usecase;

import com.unlam.verabackend.domain.port.out.AiProvider;
import com.unlam.verabackend.application.service.ExtractorService;
import com.unlam.verabackend.application.service.PromptBuilderService;
import com.unlam.verabackend.application.service.NotificationService;
import com.unlam.verabackend.application.service.ValidatorService;
import com.unlam.verabackend.domain.exception.ResourceNotFoundException;
import com.unlam.verabackend.domain.model.*;
import com.unlam.verabackend.domain.port.out.*;
import com.unlam.verabackend.infrastructure.entity.TrustContact;
import com.unlam.verabackend.infrastructure.entity.User;
import com.unlam.verabackend.infrastructure.repository.TrustContactRepository;
import com.unlam.verabackend.infrastructure.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AnalyzeContentUseCaseImplTest {

    @Mock private ValidatorService fileValidator;
    @Mock private ExtractorService urlExtractor;
    @Mock private PromptBuilderService promptBuilderService;
    @Mock private CheckUrlProvider checkUrlProvider;
    @Mock private AiProvider aiProvider;
    @Mock private UserRepository userRepository;
    @Mock private AnalysisRepository analysisRepository;
    @Mock private AlertsRepository alertsRepository;
    @Mock private TrustContactRepository trustContactRepository;
    @Mock private NotificationService notificationService;

    @Captor
    private ArgumentCaptor<Map<String, Object>> payloadCaptor;

    @InjectMocks
    private AnalyzeContentUseCaseImpl analyzeContentUseCase;

    private final String userEmail = "pepe@gmail.com";
    private User protectedUser;
    private MockMultipartFile mockFile;

    @BeforeEach
    void setUp() {
        protectedUser = new User();
        protectedUser.setId(1L);
        protectedUser.setFullName("Abuelo Pepe");
        protectedUser.setEmail(userEmail);
        protectedUser.setRole(Role.PROTECTED);

        mockFile = new MockMultipartFile("file", "test.pdf", "application/pdf", "bytes".getBytes());
    }

    @Test
    @DisplayName("Debe lanzar ResourceNotFoundException cuando el email provisto no pertenece a ningún usuario")
    void execute_WhenUserNotFound_ShouldThrowResourceNotFoundException() {
        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                analyzeContentUseCase.execute(userEmail, "text", null, "WEB")
        );
    }

    @Test
    @DisplayName("Debe lanzar IllegalArgumentException cuando el origen (source) es nulo o vacío")
    void execute_WhenSourceIsMissing_ShouldThrowIllegalArgumentException() {
        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.of(protectedUser));

        assertThrows(IllegalArgumentException.class, () ->
                analyzeContentUseCase.execute(userEmail, "text", null, null)
        );
        assertThrows(IllegalArgumentException.class, () ->
                analyzeContentUseCase.execute(userEmail, "text", null, "   ")
        );
    }

    @Test
    @DisplayName("Debe lanzar IllegalArgumentException cuando el origen provisto no coincide con ninguna constante de Source")
    void execute_WhenSourceIsInvalid_ShouldThrowIllegalArgumentException() {
        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.of(protectedUser));

        assertThrows(IllegalArgumentException.class, () ->
                analyzeContentUseCase.execute(userEmail, "text", null, "INVALID_SOURCE")
        );
    }

    @Test
    @DisplayName("Debe lanzar IllegalStateException cuando el proveedor de IA retorna un resultado nulo")
    void execute_WhenAiProviderReturnsNull_ShouldThrowIllegalStateException() {
        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.of(protectedUser));
        when(aiProvider.analyzeContent(any(), any())).thenReturn(null);

        assertThrows(IllegalStateException.class, () ->
                analyzeContentUseCase.execute(userEmail, "text", null, "WEB")
        );
    }

    @Test
    @DisplayName("Debe lanzar IllegalArgumentException si la IA responde con strings que no coinciden con los Enums")
    void execute_WhenAiResultEnumsAreCorrupt_ShouldThrowIllegalArgumentException() {
        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.of(protectedUser));

        AiResult corruptAiResult = new AiResult("Titulo", "Resumen", "RIESGO_INVENTADO", "NONE", 10, "Patrones", "Recomendacion");
        when(aiProvider.analyzeContent(any(), any())).thenReturn(corruptAiResult);

        assertThrows(IllegalArgumentException.class, () ->
                analyzeContentUseCase.execute(userEmail, "Texto", null, "WEB")
        );
    }

    @Test
    @DisplayName("Debe extraer texto y URLs exitosamente cuando se adjunta un archivo clasificado como documento")
    void execute_WhenFileIsDocumentWithUrls_ShouldExtractTextAndUrlsSuccessfully() {
        String rawText = "Texto manual";

        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.of(protectedUser));
        when(fileValidator.isDocument("test.pdf")).thenReturn(true);
        when(urlExtractor.convertDocumentToText(mockFile)).thenReturn("Contenido con http://url.com");

        when(urlExtractor.findUrls(rawText)).thenReturn(Collections.emptyList());
        when(urlExtractor.findUrls("Contenido con http://url.com")).thenReturn(List.of("http://url.com"));

        AiResult mockAiResult = new AiResult("Titulo", "Resumen", "LOW", "NONE", 10, "Patrones", "Recomendacion");
        when(aiProvider.analyzeContent(any(), any())).thenReturn(mockAiResult);
        when(analysisRepository.save(any(Analysis.class))).thenAnswer(i -> i.getArgument(0));

        Analysis result = analyzeContentUseCase.execute(userEmail, rawText, mockFile, "WEB");

        assertNotNull(result);
        verify(fileValidator, times(1)).validate(mockFile);
        verify(urlExtractor, times(1)).convertDocumentToText(mockFile);
        verify(urlExtractor, times(1)).findUrls(rawText);
        verify(urlExtractor, times(1)).findUrls("Contenido con http://url.com");
        verify(checkUrlProvider, times(1)).checkUrls(any());
    }

    @Test
    @DisplayName("Debe enviar el archivo binario completo a la IA cuando se clasifica como archivo multimedia")
    void execute_WhenFileIsMultimedia_ShouldPassFileToAiDirectly() {
        MockMultipartFile imageFile = new MockMultipartFile("file", "foto.png", "image/png", "bytes".getBytes());
        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.of(protectedUser));
        when(fileValidator.isDocument("foto.png")).thenReturn(false);
        when(fileValidator.isMultimedia("foto.png")).thenReturn(true);

        AiResult mockAiResult = new AiResult("Titulo", "Resumen", "LOW", "NONE", 0, "Patrones", "Recomendacion");
        when(aiProvider.analyzeContent(any(), eq(imageFile))).thenReturn(mockAiResult);
        when(analysisRepository.save(any(Analysis.class))).thenAnswer(i -> i.getArgument(0));

        Analysis result = analyzeContentUseCase.execute(userEmail, null, imageFile, "MOBILE");

        assertNotNull(result);
        assertEquals(Source.MOBILE, result.getSource());
        verify(fileValidator, times(1)).validate(imageFile);
        verify(urlExtractor, never()).convertDocumentToText(any());
        verify(aiProvider).analyzeContent(any(), eq(imageFile));
    }

    @Test
    @DisplayName("Debe ignorar el archivo si no es documento ni multimedia o si el archivo está vacío")
    void execute_WhenFileIsEmptyOrUnknownType_ShouldProcessTextOnly() {
        MockMultipartFile emptyFile = new MockMultipartFile("file", "", "text/plain", new byte[0]);
        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.of(protectedUser));

        AiResult mockAiResult = new AiResult("Titulo", "Resumen", "LOW", "NONE", 0, "Patrones", "Recomendacion");
        when(aiProvider.analyzeContent(any(), any())).thenReturn(mockAiResult);
        when(analysisRepository.save(any(Analysis.class))).thenAnswer(i -> i.getArgument(0));

        Analysis result = analyzeContentUseCase.execute(userEmail, "Solo texto", emptyFile, "WEB");

        assertNotNull(result);
        verify(fileValidator, never()).validate(any());
    }

    @ParameterizedTest
    @CsvSource({
            "ALTO, LOW, true",
            "ALTO, MEDIUM, true",
            "ALTO, HIGH, true",
            "MEDIO, LOW, false",
            "MEDIO, MEDIUM, true",
            "MEDIO, HIGH, true",
            "BAJO, LOW, false",
            "BAJO, MEDIUM, false",
            "BAJO, HIGH, true"
    })
    @DisplayName("Debe evaluar la matriz de alertas disparando notificaciones SSE solo bajo los cruces de sensibilidad configurados")
    void execute_WithSensitivityMatrix_ShouldConditionallyTriggerAlerts(String sensitivityStr, String riskStr, boolean expectedNotify) {
        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.of(protectedUser));

        AiResult mockAiResult = new AiResult("Titulo", "Resumen", riskStr, "CLICKED_SUSPICIOUS_LINK", 50, "Patrones", "Recomendacion");
        when(aiProvider.analyzeContent(any(), any())).thenReturn(mockAiResult);
        when(analysisRepository.save(any(Analysis.class))).thenAnswer(i -> i.getArgument(0));

        User carerUser = new User();
        carerUser.setId(2L);
        carerUser.setEmail("cuidador@gmail.com");

        TrustContact contact = new TrustContact();
        contact.setId(99L);
        contact.setSensitivityLevel(SensitivityLevel.valueOf(sensitivityStr));
        contact.setCarer(carerUser);
        contact.setProtectedUser(protectedUser);

        when(trustContactRepository.findByProtectedUserId(protectedUser.getId())).thenReturn(List.of(contact));

        if (expectedNotify) {
            Alerts mockAlert = Alerts.builder()
                    .id(UUID.randomUUID())
                    .riskLevel(RiskLevel.valueOf(riskStr))
                    .riskType(RiskType.CLICKED_SUSPICIOUS_LINK)
                    .build();
            when(alertsRepository.save(any(Alerts.class), eq(99L))).thenReturn(mockAlert);
        }

        analyzeContentUseCase.execute(userEmail, "Contenido de prueba", null, "WEB");

        if (expectedNotify) {
            verify(alertsRepository, times(1)).save(any(Alerts.class), eq(99L));
            verify(notificationService, times(1)).createAndSendNotification(eq(carerUser), eq(NotificationsType.ALERT), eq("Abuelo Pepe"), any());
        } else {
            verify(alertsRepository, never()).save(any(Alerts.class), any());
            verify(notificationService, never()).createAndSendNotification(any(), any(), any(), any());
        }
    }

    @Test
    @DisplayName("Debe finalizar el flujo sin alertas si el nivel de riesgo en el análisis es nulo")
    void execute_WhenRiskLevelIsNull_ShouldNotTriggerAlerts() {
        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.of(protectedUser));

        when(analysisRepository.save(any(Analysis.class))).thenAnswer(i -> {
            Analysis a = i.getArgument(0);
            return Analysis.builder()
                    .title(a.getTitle())
                    .riskLevel(null)
                    .riskType(a.getRiskType())
                    .build();
        });

        AiResult mockAiResult = new AiResult("Titulo", "Resumen", "LOW", "NONE", 0, "Patrones", "Recomendacion");
        when(aiProvider.analyzeContent(any(), any())).thenReturn(mockAiResult);

        Analysis result = analyzeContentUseCase.execute(userEmail, "Mensaje", null, "WEB");

        assertNotNull(result);
        verify(trustContactRepository, never()).findByProtectedUserId(any());
    }

    @Test
    @DisplayName("Debe finalizar el flujo sin alertas si el rol del usuario que analiza es CARER")
    void execute_WhenUserIsCarer_ShouldNotTriggerAlerts() {
        User carerUser = new User();
        carerUser.setId(5L);
        carerUser.setEmail("cuidador.activo@gmail.com");
        carerUser.setRole(Role.CARER);

        when(userRepository.findByEmail("cuidador.activo@gmail.com")).thenReturn(Optional.of(carerUser));

        AiResult mockAiResult = new AiResult("Titulo", "Resumen", "HIGH", "IDENTITY_THEFT_OR_IMPERSONATION", 80, "Patrones", "Recomendacion");
        when(aiProvider.analyzeContent(any(), any())).thenReturn(mockAiResult);
        when(analysisRepository.save(any(Analysis.class))).thenAnswer(i -> i.getArgument(0));

        Analysis result = analyzeContentUseCase.execute("cuidador.activo@gmail.com", "Mensaje", null, "WEB");

        assertNotNull(result);
        verify(trustContactRepository, never()).findByProtectedUserId(any());
        verify(alertsRepository, never()).save(any(), anyLong());
        verify(notificationService, never()).createAndSendNotification(any(), any(), any(), any());
    }

    @Test
    @DisplayName("Debe adjuntar el ID de la alerta y su respectivo nivel de riesgo dentro del payload enviado por SSE")
    void execute_WhenNotificationIsSent_ShouldContainCorrectPayloadData() {
        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.of(protectedUser));

        AiResult mockAiResult = new AiResult("Peligro", "Resumen", "HIGH", "IDENTITY_THEFT_OR_IMPERSONATION", 95, "Patrones", "Recomendacion");
        when(aiProvider.analyzeContent(any(), any())).thenReturn(mockAiResult);
        when(analysisRepository.save(any(Analysis.class))).thenAnswer(i -> i.getArgument(0));

        User carerUser = new User();
        carerUser.setId(3L);

        TrustContact contact = new TrustContact();
        contact.setId(10L);
        contact.setSensitivityLevel(SensitivityLevel.BAJO);
        contact.setCarer(carerUser);

        when(trustContactRepository.findByProtectedUserId(protectedUser.getId())).thenReturn(List.of(contact));

        UUID alertUuid = UUID.randomUUID();
        Alerts mockAlert = Alerts.builder()
                .id(alertUuid)
                .riskLevel(RiskLevel.HIGH)
                .riskType(RiskType.IDENTITY_THEFT_OR_IMPERSONATION)
                .build();
        when(alertsRepository.save(any(Alerts.class), eq(10L))).thenReturn(mockAlert);

        analyzeContentUseCase.execute(userEmail, "Alerta máxima", null, "WEB");

        verify(notificationService).createAndSendNotification(
                eq(carerUser),
                eq(NotificationsType.ALERT),
                eq("Abuelo Pepe"),
                payloadCaptor.capture()
        );

        Map<String, Object> capturedPayload = payloadCaptor.getValue();
        assertNotNull(capturedPayload);
        assertEquals(alertUuid.toString(), capturedPayload.get("alertId"));
        assertEquals("HIGH", capturedPayload.get("riskLevel"));
    }
}