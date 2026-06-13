package com.unlam.verabackend.application.usecase;

import com.unlam.verabackend.application.service.ExtractorService;
import com.unlam.verabackend.application.service.SseService;
import com.unlam.verabackend.application.service.PromptBuilderService;
import com.unlam.verabackend.application.service.ValidatorService;
import com.unlam.verabackend.domain.exception.ResourceNotFoundException;
import com.unlam.verabackend.domain.model.*;
import com.unlam.verabackend.domain.port.out.*;
import com.unlam.verabackend.infrastructure.entity.TrustContact;
import com.unlam.verabackend.infrastructure.entity.User;
import com.unlam.verabackend.infrastructure.repository.TrustContactRepository;
import com.unlam.verabackend.infrastructure.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
    @Mock private SafeBrowsingProvider safeBrowsingProvider;
    @Mock private GeminiProvider geminiProvider;
    @Mock private UserRepository userRepository;
    @Mock private AnalysisRepository analysisRepository;
    @Mock private AlertsRepository alertsRepository;
    @Mock private TrustContactRepository trustContactRepository;
    @Mock private SseService sseService;

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

        mockFile = new MockMultipartFile("file", "test.pdf", "application/pdf", "bytes".getBytes());
    }

    // ==========================================
    // 1. Tests de Excepciones y Cláusulas de Guarda
    // ==========================================

    @Test
    void execute_WhenUserNotFound_ShouldThrowResourceNotFoundException() {
        // Arrange
        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () ->
                analyzeContentUseCase.execute(userEmail, "text", null, "WEB")
        );
    }

    @Test
    void execute_WhenBothTextAndFileAreMissing_ShouldThrowIllegalArgumentException() {
        // Arrange
        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.of(protectedUser));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                analyzeContentUseCase.execute(userEmail, null, null, "WEB")
        );
        assertEquals("Debe ingresar un texto o adjuntar un archivo para comenzar el análisis.", exception.getMessage());
    }

    @Test
    void execute_WhenSourceIsMissing_ShouldThrowIllegalArgumentException() {
        // Arrange
        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.of(protectedUser));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () ->
                analyzeContentUseCase.execute(userEmail, "text", null, null)
        );
        assertThrows(IllegalArgumentException.class, () ->
                analyzeContentUseCase.execute(userEmail, "text", null, "   ")
        );
    }

    @Test
    void execute_WhenGeminiReturnsNull_ShouldThrowIllegalStateException() {
        // Arrange
        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.of(protectedUser));
        when(geminiProvider.analyzeContent(any(), any())).thenReturn(null);

        // Act & Assert
        assertThrows(IllegalStateException.class, () ->
                analyzeContentUseCase.execute(userEmail, "text", null, "WEB")
        );
    }

    // ==========================================
    // 2. Cobertura de Caminos de Archivos (Documentos vs Multimedia)
    // ==========================================

    @Test
    void execute_WhenFileIsDocumentWithUrls_ShouldExtractTextAndUrlsSuccessfully() {
        // Arrange
        String rawText = "Texto manual";

        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.of(protectedUser));
        when(fileValidator.isDocument("test.pdf")).thenReturn(true);
        when(urlExtractor.convertDocumentToText(mockFile)).thenReturn("Contenido con http://url.com");

        when(urlExtractor.findUrls(rawText)).thenReturn(Collections.emptyList());
        when(urlExtractor.findUrls("Contenido con http://url.com")).thenReturn(List.of("http://url.com"));

        GeminiResult mockAiResult = new GeminiResult("Titulo", "Resumen", "LOW", "NONE", 10, "Patrones", "Recomendacion");
        when(geminiProvider.analyzeContent(any(), any())).thenReturn(mockAiResult);
        when(analysisRepository.save(any(Analysis.class))).thenAnswer(i -> i.getArgument(0));

        // Act
        Analysis result = analyzeContentUseCase.execute(userEmail, rawText, mockFile, "WEB");

        // Assert
        assertNotNull(result);
        verify(urlExtractor, times(1)).convertDocumentToText(mockFile);
        verify(urlExtractor, times(1)).findUrls(rawText);
        verify(urlExtractor, times(1)).findUrls("Contenido con http://url.com");
        verify(safeBrowsingProvider, times(1)).checkUrls(any());
    }

    @Test
    void execute_WhenFileIsMultimedia_ShouldPassFileToGeminiDirectly() {
        // Arrange
        MockMultipartFile imageFile = new MockMultipartFile("file", "foto.png", "image/png", "bytes".getBytes());
        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.of(protectedUser));
        when(fileValidator.isDocument("foto.png")).thenReturn(false);
        when(fileValidator.isMultimedia("foto.png")).thenReturn(true);

        GeminiResult mockAiResult = new GeminiResult("Titulo", "Resumen", "LOW", "NONE", 0, "Patrones", "Recomendacion");
        when(geminiProvider.analyzeContent(any(), eq(imageFile))).thenReturn(mockAiResult);
        when(analysisRepository.save(any(Analysis.class))).thenAnswer(i -> i.getArgument(0));

        // Act
        Analysis result = analyzeContentUseCase.execute(userEmail, null, imageFile, "MOBILE");

        // Assert
        assertNotNull(result);
        assertEquals(Source.MOBILE, result.getSource());
        verify(geminiProvider).analyzeContent(any(), eq(imageFile));
    }

    // ==========================================
    // 3. Flujo Crítico: Riesgo Alto, Alertas y Notificaciones
    // ==========================================

    @Test
    void execute_WhenRiskIsHigh_ShouldCreateAlertsAndSendNotificationsToCarers() {
        // Arrange
        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.of(protectedUser));

        GeminiResult mockHighRiskResult = new GeminiResult("Peligro", "Fraude", " HIGH ", " PHISHING ", 90, "Patrones", "Recomendacion");
        when(geminiProvider.analyzeContent(any(), any())).thenReturn(mockHighRiskResult);

        Analysis simulatedSavedAnalysis = Analysis.builder()
                .title("Peligro").source(Source.WEB).riskLevel(RiskLevel.HIGH).riskType(RiskType.PHISHING).riskPercentage(90).build();
        when(analysisRepository.save(any(Analysis.class))).thenReturn(simulatedSavedAnalysis);

        User carerUser = new User();
        carerUser.setId(2L);
        TrustContact contact = TrustContact.builder().id(77L).carer(carerUser).protectedUser(protectedUser).build();
        when(trustContactRepository.findByProtectedUserId(protectedUser.getId())).thenReturn(List.of(contact));

        Alerts simulatedSavedAlert = Alerts.builder().id(UUID.randomUUID()).build();
        when(alertsRepository.save(any(Alerts.class), eq(77L))).thenReturn(simulatedSavedAlert);

        // Act
        Analysis result = analyzeContentUseCase.execute(userEmail, "Texto peligroso", null, "WEB");

        // Assert
        assertNotNull(result);
        assertEquals(RiskLevel.HIGH, result.getRiskLevel());

        verify(alertsRepository, times(1)).save(any(Alerts.class), eq(77L));

        verify(sseService, times(1)).createAndSendNotification(
                eq(carerUser),
                eq(NotificationsType.ALERT),
                eq(protectedUser.getFullName()),
                payloadCaptor.capture()
        );

        assertNotNull(payloadCaptor.getValue().get("alertId"));
    }
}