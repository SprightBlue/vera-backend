//package com.unlam.verabackend.application.usecase;
//
//import com.unlam.verabackend.domain.model.*;
//import com.unlam.verabackend.domain.ports.out.*;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//
//import java.time.LocalDateTime;
//import java.util.List;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.Mockito.*;
//
//@ExtendWith(MockitoExtension.class)
//public class AnalyzeMessageUseCaseImplTest {
//
//    @Mock
//    private AnalysisRepository analysisRepository;
//    @Mock
//    private RiskAlertRepository riskAlertRepository;
//    @Mock
//    private UserCaregiverRepository userCaregiverRepository;
//    @Mock
//    private SafeBrowsingApiPort safeBrowsingApiPort;
//    @Mock
//    private GeminiApiPort geminiApiPort;
//
//    @InjectMocks
//    private AnalyzeMessageUseCaseImpl analyzeMessageUseCase;
//
//    private DomainUser mockUser;
//    private UrlValidation cleanUrlValidation;
//    private UrlValidation maliciousUrlValidation;
//    private MessageAssessment lowRiskAssessment;
//    private MessageAssessment highRiskAssessment;
//
//    @BeforeEach
//    void setUp() {
//        mockUser = new DomainUser(1L, "Juan Pérez", "juan@mail.com", Role.ROLE_USER, LocalDateTime.now(), LocalDateTime.now(), true, true);
//        cleanUrlValidation = new UrlValidation(false, List.of());
//        maliciousUrlValidation = new UrlValidation(true, List.of("MALWARE"));
//
//        lowRiskAssessment = new MessageAssessment(RiskLevel.LOW.name(), "Ninguno", "Todo ok");
//        highRiskAssessment = new MessageAssessment(RiskLevel.HIGH.name(), "Patrón sospechoso", "Ojo, no respondas");
//    }
//
//    @Test
//    void shouldSaveAnalysisAndNotDispatchAlertsWhenRiskIsLow() {
//        String content = "Hola, cómo estás?";
//        when(safeBrowsingApiPort.checkUrlsInContent(content)).thenReturn(cleanUrlValidation);
//        when(geminiApiPort.analyzeMessageContent(content, cleanUrlValidation)).thenReturn(lowRiskAssessment);
//
//        Analysis result = analyzeMessageUseCase.analyzeMessage(mockUser, content, MessageSource.WHATSAPP);
//
//        assertNotNull(result);
//        assertEquals(RiskLevel.LOW, result.getRiskLevel());
//        verify(analysisRepository, times(1)).save(any(Analysis.class));
//        verify(userCaregiverRepository, never()).findByUserId(anyLong());
//        verify(riskAlertRepository, never()).save(any(RiskAlert.class));
//    }
//
//    @Test
//    void shouldSaveAnalysisAndDispatchAlertsToCaregiverWhenRiskIsHigh() {
//        String content = "Urgente, dame tu clave bancaria";
//        DomainUser mockCaregiver = new DomainUser(2L, "Pedro Cuidador", "pedro@mail.com", Role.ROLE_ADMIN, LocalDateTime.now(), LocalDateTime.now(), true, true);
//
//        UserCaregiver mockRelation = new UserCaregiver(
//                1L,
//                mockUser,
//                mockCaregiver,
//                RelationshipType.FAMILY_MEMBER,
//                "+541112345678",
//                "pedro@mail.com",
//                LocalDateTime.now()
//        );
//
//        when(safeBrowsingApiPort.checkUrlsInContent(content)).thenReturn(cleanUrlValidation);
//        when(geminiApiPort.analyzeMessageContent(content, cleanUrlValidation)).thenReturn(highRiskAssessment);
//        when(userCaregiverRepository.findByUserId(mockUser.getId())).thenReturn(List.of(mockRelation));
//
//        Analysis result = analyzeMessageUseCase.analyzeMessage(mockUser, content, MessageSource.TELEGRAM);
//
//        assertNotNull(result);
//        assertEquals(RiskLevel.HIGH, result.getRiskLevel());
//        verify(analysisRepository, times(1)).save(any(Analysis.class));
//        verify(userCaregiverRepository, times(1)).findByUserId(mockUser.getId());
//        verify(riskAlertRepository, times(1)).save(any(RiskAlert.class));
//    }
//
//    @Test
//    void shouldPassDangerFlagToAnalyzerWhenUrlIsMalicious() {
//        String content = "Gané un premio acá: http://link-clonado.com";
//        when(safeBrowsingApiPort.checkUrlsInContent(content)).thenReturn(maliciousUrlValidation);
//        when(geminiApiPort.analyzeMessageContent(content, maliciousUrlValidation)).thenReturn(highRiskAssessment);
//        when(userCaregiverRepository.findByUserId(mockUser.getId())).thenReturn(List.of());
//
//        Analysis result = analyzeMessageUseCase.analyzeMessage(mockUser, content, MessageSource.WHATSAPP);
//
//        assertNotNull(result);
//        verify(safeBrowsingApiPort, times(1)).checkUrlsInContent(content);
//        verify(geminiApiPort, times(1)).analyzeMessageContent(content, maliciousUrlValidation);
//    }
//
//    @Test
//    void shouldPassCleanFlagToAnalyzerWhenUrlIsSafe() {
//        String content = "Mirá este video de gatitos: https://youtube.com";
//        when(safeBrowsingApiPort.checkUrlsInContent(content)).thenReturn(cleanUrlValidation);
//        when(geminiApiPort.analyzeMessageContent(content, cleanUrlValidation)).thenReturn(lowRiskAssessment);
//
//        Analysis result = analyzeMessageUseCase.analyzeMessage(mockUser, content, MessageSource.WHATSAPP);
//
//        assertNotNull(result);
//        assertEquals(RiskLevel.LOW, result.getRiskLevel());
//        verify(safeBrowsingApiPort, times(1)).checkUrlsInContent(content);
//        verify(geminiApiPort, times(1)).analyzeMessageContent(content, cleanUrlValidation);
//    }
//
//    @Test
//    void shouldThrowExceptionWhenContentIsEmpty() {
//        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> analyzeMessageUseCase.analyzeMessage(mockUser, "", MessageSource.WHATSAPP));
//
//        assertEquals("El contenido no puede estar vacío", exception.getMessage());
//    }
//}
