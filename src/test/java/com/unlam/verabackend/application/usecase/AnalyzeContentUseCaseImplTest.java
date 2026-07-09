package com.unlam.verabackend.application.usecase;

import com.unlam.verabackend.application.service.*;
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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Pruebas Unitarias para AnalyzeContentUseCaseImpl")
class AnalyzeContentUseCaseImplTest {

    @Mock private ValidatorService fileValidator;
    @Mock private ExtractorService urlExtractor;
    @Mock private CheckUrlProvider checkUrlProvider;
    @Mock private PromptBuilderService promptBuilder;
    @Mock private AiProvider aiProvider;
    @Mock private UserRepository userRepository;
    @Mock private AnalysisRepository analysisRepository;
    @Mock private AlertsRepository alertsRepository;
    @Mock private TrustContactRepository trustContactRepository;
    @Mock private NotificationService notificationService;
    @Mock private RtcProvider rtcProvider;

    @InjectMocks private AnalyzeContentUseCaseImpl analyzeContentUseCase;

    private String userEmail;
    private User mockProtectedUser;
    private User mockCarerUser;
    private AiResult mockAiResult;
    private MultipartFile mockFile;

    @BeforeEach
    void setUp() {
        userEmail = "user@unlam.edu.ar";

        mockProtectedUser = new User();
        mockProtectedUser.setId(1L);
        mockProtectedUser.setEmail(userEmail);
        mockProtectedUser.setRole(Role.PROTECTED);
        mockProtectedUser.setFullName("Protected User");

        mockCarerUser = new User();
        mockCarerUser.setId(2L);
        mockCarerUser.setEmail("carer@unlam.edu.ar");
        mockCarerUser.setRole(Role.CARER);

        mockAiResult = new AiResult(
                "Posible estafa",
                "Resumen",
                "HIGH",
                "CLICKED_SUSPICIOUS_LINK",
                95,
                "Uso de urgencia y enlaces raros",
                "No ingresar datos"
        );

        mockFile = mock(MultipartFile.class);
    }

    @Test
    @DisplayName("Debería ejecutar análisis de forma exitosa y disparar alertas al tutor si el emisor es Protegido y el riesgo es Alto")
    void execute_ProtectedUser_HighRisk_ShouldDispatchAlert() {
        // Arrange
        String rawText = "Ganaste un premio, entra a http://malicioso.com";
        List<String> mockUrls = List.of("http://malicioso.com");

        TrustContact contact = new TrustContact();
        contact.setId(100L);
        contact.setSensitivityLevel(SensitivityLevel.BAJO);
        contact.setCarer(mockCarerUser);
        contact.setProtectedUser(mockProtectedUser);

        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.of(mockProtectedUser));
        when(urlExtractor.findUrls(rawText)).thenReturn(mockUrls);
        when(checkUrlProvider.checkUrls(mockUrls)).thenReturn(List.of("MATCH"));
        when(mockFile.isEmpty()).thenReturn(false);
        when(mockFile.getOriginalFilename()).thenReturn("evidencia.jpg");
        when(fileValidator.isMultimedia("evidencia.jpg")).thenReturn(true);

        when(promptBuilder.buildPrompt(any(), any(), any(), any())).thenReturn("Generated Prompt");
        when(aiProvider.analyzeContent(eq("Generated Prompt"), eq(mockFile))).thenReturn(mockAiResult);

        when(analysisRepository.save(any(Analysis.class))).thenAnswer(inv -> {
            Analysis a = inv.getArgument(0);
            a.setId(UUID.randomUUID());
            return a;
        });

        when(trustContactRepository.findByProtectedUserId(mockProtectedUser.getId())).thenReturn(List.of(contact));

        when(alertsRepository.save(any(Alerts.class), eq(100L))).thenAnswer(inv -> {
            Alerts al = inv.getArgument(0);
            al.setId(UUID.randomUUID());

            var domainTrustContact = TrustContact.builder()
                    .id(contact.getId())
                    .sensitivityLevel(contact.getSensitivityLevel())
                    .carer(User.builder()
                            .id(mockCarerUser.getId())
                            .email(mockCarerUser.getEmail())
                            .role(mockCarerUser.getRole())
                            .build())
                    .protectedUser(User.builder()
                            .id(mockProtectedUser.getId())
                            .email(mockProtectedUser.getEmail())
                            .fullName(mockProtectedUser.getFullName())
                            .build())
                    .build();

            al.setTrustContact(domainTrustContact);
            return al;
        });

        doNothing().when(rtcProvider).publishCarerDashboardAlertUpdate(anyString(), any(Alerts.class));

        // Act
        Analysis result = analyzeContentUseCase.execute(userEmail, rawText, mockFile, "MOBILE");

        // Assert
        assertNotNull(result);
        assertEquals(RiskLevel.HIGH, result.getRiskLevel());
        verify(alertsRepository, times(1)).save(any(Alerts.class), eq(100L));
        verify(notificationService, times(1)).createAndDispatch(eq(mockCarerUser), eq(NotificationsType.ALERT), eq("Protected User"), anyMap());
        verify(rtcProvider, times(1)).publishCarerDashboardAlertUpdate(eq("carer@unlam.edu.ar"), any(Alerts.class));
    }

    @Test
    @DisplayName("Debería procesar archivos de tipo Documento extrayendo texto plano interno y buscando URLs")
    void execute_DocumentFile_ShouldExtractInternalText() {
        // Arrange
        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.of(mockCarerUser));
        when(mockFile.isEmpty()).thenReturn(false);
        when(mockFile.getOriginalFilename()).thenReturn("resumen.pdf");
        when(fileValidator.isDocument("resumen.pdf")).thenReturn(true);
        when(urlExtractor.convertDocumentToText(mockFile)).thenReturn("Contenido PDF con http://linkdoc.com");
        when(urlExtractor.findUrls("Contenido PDF con http://linkdoc.com")).thenReturn(List.of("http://linkdoc.com"));

        when(aiProvider.analyzeContent(any(), any())).thenReturn(mockAiResult);
        when(analysisRepository.save(any(Analysis.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        Analysis result = analyzeContentUseCase.execute(userEmail, null, mockFile, "WEB");

        // Assert
        assertNotNull(result);
        verify(urlExtractor, times(1)).convertDocumentToText(mockFile);
        verify(checkUrlProvider, times(1)).checkUrls(List.of("http://linkdoc.com"));
    }

    @Test
    @DisplayName("Debería omitir el despacho de alertas si el análisis fue iniciado legítimamente por un Cuidador")
    void execute_CarerUser_ShouldSuppressAlertsTree() {
        // Arrange
        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.of(mockCarerUser));
        when(aiProvider.analyzeContent(any(), any())).thenReturn(mockAiResult);
        when(analysisRepository.save(any(Analysis.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        Analysis result = analyzeContentUseCase.execute(userEmail, "Texto plano", null, "MOBILE");

        // Assert
        assertNotNull(result);
        verify(trustContactRepository, never()).findByProtectedUserId(anyLong());
        verify(alertsRepository, never()).save(any(), anyLong());
    }

    @Test
    @DisplayName("Debería evaluar de forma precisa la matriz de sensibilidad Medio y Bajo ante diferentes riesgos")
    void execute_MatrixEvaluation_ShouldFilterAlertsCorrectly() {
        // Arrange
        AiResult mediumRiskResult = new AiResult(
                "Titulo",
                "Resumen",
                "MEDIUM",
                "IDENTITY_THEFT_OR_IMPERSONATION",
                50,
                "Patrón de clonación de identidad",
                "Rec"
        );

        TrustContact contactMedio = new TrustContact();
        contactMedio.setId(201L);
        contactMedio.setSensitivityLevel(SensitivityLevel.MEDIO);
        contactMedio.setCarer(mockCarerUser);
        contactMedio.setProtectedUser(mockProtectedUser);

        TrustContact contactBajo = new TrustContact();
        contactBajo.setId(202L);
        contactBajo.setSensitivityLevel(SensitivityLevel.BAJO);
        contactBajo.setCarer(mockCarerUser);
        contactBajo.setProtectedUser(mockProtectedUser);

        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.of(mockProtectedUser));
        when(aiProvider.analyzeContent(any(), any())).thenReturn(mediumRiskResult);
        when(analysisRepository.save(any(Analysis.class))).thenAnswer(inv -> inv.getArgument(0));
        when(trustContactRepository.findByProtectedUserId(mockProtectedUser.getId())).thenReturn(List.of(contactMedio, contactBajo));

        when(alertsRepository.save(any(Alerts.class), eq(201L))).thenAnswer(inv -> {
            Alerts alert = inv.getArgument(0);
            alert.setId(UUID.randomUUID());

            alert.setTrustContact(TrustContact.builder()
                    .carer(User.builder().email("carer@unlam.edu.ar").build())
                    .build());
            return alert;
        });

        doNothing().when(rtcProvider).publishCarerDashboardAlertUpdate(anyString(), any(Alerts.class));

        // Act
        analyzeContentUseCase.execute(userEmail, "Texto", null, "WEB");

        // Assert
        verify(alertsRepository, times(1)).save(any(Alerts.class), eq(201L));
        verify(alertsRepository, never()).save(any(Alerts.class), eq(202L));
        verify(rtcProvider, times(1)).publishCarerDashboardAlertUpdate(eq("carer@unlam.edu.ar"), any(Alerts.class));
    }

    @Test
    @DisplayName("Debería lanzar ResourceNotFoundException si el email analista no está indexado")
    void execute_UserNotFound_ShouldThrowResourceNotFoundException() {
        // Arrange
        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () ->
                analyzeContentUseCase.execute(userEmail, "Texto", null, "MOBILE")
        );
    }

    @Test
    @DisplayName("Debería lanzar IllegalArgumentException si la procedencia (Source) es nula o vacía")
    void execute_SourceInvalid_ShouldThrowIllegalArgumentException() {
        // Arrange
        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.of(mockProtectedUser));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () ->
                analyzeContentUseCase.execute(userEmail, "Texto", null, "   ")
        );
    }

    @Test
    @DisplayName("Debería lanzar IllegalStateException si la respuesta obtenida del motor de IA es nula")
    void execute_AiProviderReturnsNull_ShouldThrowIllegalStateException() {
        // Arrange
        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.of(mockProtectedUser));
        when(aiProvider.analyzeContent(any(), any())).thenReturn(null);

        // Act & Assert
        assertThrows(IllegalStateException.class, () ->
                analyzeContentUseCase.execute(userEmail, "Texto", null, "MOBILE")
        );
    }

    @Test
    @DisplayName("Debería salir de forma temprana y no alertar si el nivel de riesgo de la entidad guardada resulta nulo")
    void processSecurityAlerts_RiskLevelNull_ShouldExitEarly() {
        // Arrange
        AiResult corruptResult = new AiResult("T", "R", "LOW", "NONE", 0, "Ninguno", "Rec");

        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.of(mockProtectedUser));
        when(aiProvider.analyzeContent(any(), any())).thenReturn(corruptResult);

        Analysis analysisWithNullRisk = new Analysis();
        analysisWithNullRisk.setRiskLevel(null);
        when(analysisRepository.save(any(Analysis.class))).thenReturn(analysisWithNullRisk);

        // Act
        Analysis result = analyzeContentUseCase.execute(userEmail, "Texto", null, "MOBILE");

        // Assert
        assertNull(result.getRiskLevel());
        verify(trustContactRepository, never()).findByProtectedUserId(anyLong());
    }
}