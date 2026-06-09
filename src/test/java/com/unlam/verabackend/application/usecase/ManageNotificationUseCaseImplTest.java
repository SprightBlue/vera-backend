//package com.unlam.verabackend.application.usecase;
//
//import com.unlam.verabackend.domain.model.*;
//import com.unlam.verabackend.domain.ports.out.LinkGeneratorService;
//import com.unlam.verabackend.domain.ports.out.RiskAlertRepository;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//
//import java.time.LocalDateTime;
//import java.util.List;
//import java.util.Optional;
//import java.util.UUID;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.Mockito.*;
//
//@ExtendWith(MockitoExtension.class)
//public class ManageRiskAlertUseCaseImplTest {
//
//    @Mock
//    private RiskAlertRepository riskAlertRepository;
//    @Mock
//    private LinkGeneratorService linkGeneratorService;
//
//    @InjectMocks
//    private ManageRiskAlertUseCaseImpl manageRiskAlertUseCase;
//
//    private RiskAlert mockAlert;
//    private String mockAlertId;
//
//    @BeforeEach
//    void setUp() {
//        mockAlertId = UUID.randomUUID().toString();
//        DomainUser mockElderly = new DomainUser(1L, "Abuelo", "abuelo@mail.com", Role.ROLE_USER, LocalDateTime.now(), LocalDateTime.now(), true, true);
//        DomainUser mockCaregiver = new DomainUser(2L, "Cuidador", "cuidador@mail.com", Role.ROLE_ADMIN, LocalDateTime.now(), LocalDateTime.now(), true, true);
//
//        Analysis mockAnalysis = new Analysis(UUID.randomUUID(), mockElderly, "Contenido sospechoso", MessageSource.TELEGRAM, RiskLevel.HIGH, "patron", "recom", LocalDateTime.now());
//
//        mockAlert = new RiskAlert(UUID.fromString(mockAlertId), mockAnalysis, mockCaregiver, false, LocalDateTime.now());
//    }
//
//    @Test
//    void shouldReturnActiveAlertsByCaregiver() {
//        Long caregiverId = 2L;
//        when(riskAlertRepository.findActiveByCaregiver(caregiverId)).thenReturn(List.of(mockAlert));
//
//        List<RiskAlert> activeAlerts = manageRiskAlertUseCase.getActiveAlertsByCaregiver(caregiverId);
//
//        assertNotNull(activeAlerts);
//        assertEquals(1, activeAlerts.size());
//        verify(riskAlertRepository, times(1)).findActiveByCaregiver(caregiverId);
//    }
//
//    @Test
//    void shouldMarkAlertAsSolvedWhenAlertExists() {
//        when(riskAlertRepository.findById(mockAlertId)).thenReturn(Optional.of(mockAlert));
//
//        manageRiskAlertUseCase.markAlertAsSolved(mockAlertId);
//
//        assertTrue(mockAlert.isSolved());
//        verify(riskAlertRepository, times(1)).save(mockAlert);
//    }
//
//    @Test
//    void shouldThrowRuntimeExceptionWhenMarkingNonExistentAlertAsSolved() {
//        String nonExistentId = "invalid-id";
//        when(riskAlertRepository.findById(nonExistentId)).thenReturn(Optional.empty());
//
//        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
//            manageRiskAlertUseCase.markAlertAsSolved(nonExistentId);
//        });
//
//        assertTrue(exception.getMessage().contains("No se encontró la alerta de riesgo con ID:"));
//        verify(riskAlertRepository, never()).save(any(RiskAlert.class));
//    }
//
//    @Test
//    void shouldGenerateContactLinkCorrectly() {
//        String expectedLink = "mailto:abuelo@mail.com?subject=Seguimiento...";
//        when(riskAlertRepository.findById(mockAlertId)).thenReturn(Optional.of(mockAlert));
//        when(linkGeneratorService.generateEmailLink(eq("abuelo@mail.com"), anyString())).thenReturn(expectedLink);
//
//        String resultLink = manageRiskAlertUseCase.getContactLinkForUser(mockAlertId);
//
//        assertNotNull(resultLink);
//        assertEquals(expectedLink, resultLink);
//        verify(linkGeneratorService, times(1)).generateEmailLink(eq("abuelo@mail.com"), anyString());
//    }
//}
