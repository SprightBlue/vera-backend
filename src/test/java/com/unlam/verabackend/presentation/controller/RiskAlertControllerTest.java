//package com.unlam.verabackend.presentation.controller;
//
//import com.unlam.verabackend.domain.exception.ResourceNotFoundException;
//import com.unlam.verabackend.domain.model.*;
//import com.unlam.verabackend.domain.ports.in.GetAlertDetailUseCase;
//import com.unlam.verabackend.domain.ports.in.ManageRiskAlertUseCase;
//import com.unlam.verabackend.presentation.dto.AlertDetailPresentation;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//
//import java.time.LocalDateTime;
//import java.util.List;
//import java.util.UUID;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.Mockito.*;
//
//@ExtendWith(MockitoExtension.class)
//class RiskAlertControllerTest {
//
//    @Mock
//    private ManageRiskAlertUseCase manageRiskAlertUseCase;
//
//    @Mock
//    private GetAlertDetailUseCase getAlertDetailUseCase;
//
//    @InjectMocks
//    private RiskAlertController riskAlertController;
//
//    private RiskAlert mockAlert;
//    private String mockAlertId;
//    private UUID alertUuid;
//    private Long caregiverId;
//
//    @BeforeEach
//    void setUp() {
//        alertUuid = UUID.randomUUID();
//        mockAlertId = alertUuid.toString();
//        caregiverId = 2L;
//        UUID mockAnalysisId = UUID.randomUUID();
//
//        DomainUser mockElderly = new DomainUser(1L, "Abuelo Juan", "abuelo@mail.com", Role.ROLE_USER, LocalDateTime.now(), LocalDateTime.now(), true, true);
//        DomainUser mockCaregiver = new DomainUser(2L, "Cuidador Pedro", "pedro@mail.com", Role.ROLE_ADMIN, LocalDateTime.now(), LocalDateTime.now(), true, true);
//
//        Analysis mockAnalysis = new Analysis(mockAnalysisId, mockElderly, "Contenido sospechoso", MessageSource.TELEGRAM, RiskLevel.HIGH, "patron", "recom", LocalDateTime.now());
//
//        mockAlert = new RiskAlert(alertUuid, mockAnalysis, mockCaregiver, false, LocalDateTime.now());
//    }
//
//    @Test
//    void shouldReturnActiveAlertsList() {
//        Long caregiverId = 2L;
//        when(manageRiskAlertUseCase.getActiveAlertsByCaregiver(caregiverId)).thenReturn(List.of(mockAlert));
//
//        ResponseEntity<List<RiskAlertController.RiskAlertResponse>> responseEntity = riskAlertController.getActiveAlerts(caregiverId);
//
//        assertNotNull(responseEntity);
//        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
//
//        List<RiskAlertController.RiskAlertResponse> body = responseEntity.getBody();
//        assertNotNull(body);
//        assertEquals(1, body.size());
//
//        assertEquals(mockAlertId, body.getFirst().alertId());
//        assertEquals("Abuelo Juan", body.getFirst().protectedUserName());
//        assertEquals("Contenido sospechoso", body.getFirst().messageContent());
//        assertEquals("Telegram", body.getFirst().source());
//        assertEquals("HIGH", body.getFirst().riskLevel());
//        assertEquals("patron", body.getFirst().suspiciousPatterns());
//
//        verify(manageRiskAlertUseCase, times(1)).getActiveAlertsByCaregiver(caregiverId);
//    }
//
//    @Test
//    void shouldReturnNoContentWhenAlertIsMarkedAsSolved() {
//        doNothing().when(manageRiskAlertUseCase).markAlertAsSolved(mockAlertId);
//
//        ResponseEntity<Void> responseEntity = riskAlertController.solveAlert(mockAlertId);
//
//        assertNotNull(responseEntity);
//        assertEquals(HttpStatus.NO_CONTENT, responseEntity.getStatusCode());
//
//        verify(manageRiskAlertUseCase, times(1)).markAlertAsSolved(mockAlertId);
//    }
//
//    @Test
//    void shouldReturnContactMailtoLink() {
//        String expectedMailto = "mailto:abuelo@mail.com?subject=Seguimiento";
//        when(manageRiskAlertUseCase.getContactLinkForUser(mockAlertId)).thenReturn(expectedMailto);
//
//        ResponseEntity<RiskAlertController.ContactLinkResponse> responseEntity = riskAlertController.getContactLink(mockAlertId);
//
//        assertNotNull(responseEntity);
//        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
//
//        RiskAlertController.ContactLinkResponse body = responseEntity.getBody();
//        assertNotNull(body);
//        assertEquals(expectedMailto, body.link());
//
//        verify(manageRiskAlertUseCase, times(1)).getContactLinkForUser(mockAlertId);
//    }
//
//    @Test
//    void shouldReturnAlertDetailPresentationWhenAuthorized() {
//        AlertDetail domainDetail = new AlertDetail(
//                alertUuid,
//                mockAlert.getAnalysis().getId(),
//                mockAlert.getAnalysis().getContent(),
//                mockAlert.getAnalysis().getMessageSource(),
//                mockAlert.getAnalysis().getRiskLevel(),
//                mockAlert.getAnalysis().getSuspiciousPatterns(),
//                mockAlert.getAnalysis().getRecommendation(),
//                false,
//                mockAlert.getCreatedAt()
//        );
//
//        when(getAlertDetailUseCase.getDetail(alertUuid, caregiverId)).thenReturn(domainDetail);
//
//        ResponseEntity<?> response = riskAlertController.getDetail(alertUuid, caregiverId);
//
//        assertEquals(HttpStatus.OK, response.getStatusCode());
//        assertNotNull(response.getBody());
//        assertInstanceOf(AlertDetailPresentation.class, response.getBody());
//
//        AlertDetailPresentation body = (AlertDetailPresentation) response.getBody();
//        assertEquals(alertUuid, body.alertId());
//        assertEquals(mockAlert.getAnalysis().getId(), body.analysisId());
//        assertEquals("Contenido sospechoso", body.messageContent());
//        assertEquals("TELEGRAM", body.messageSource());
//        assertEquals("HIGH", body.riskLevel());
//        assertEquals("Alto", body.riskLevelDisplayName());
//        assertEquals("patron", body.suspiciousPatterns());
//        assertEquals("recom", body.recommendation());
//        assertFalse(body.received());
//        assertEquals(mockAlert.getCreatedAt(), body.createdAt());
//
//        verify(getAlertDetailUseCase, times(1)).getDetail(alertUuid, caregiverId);
//    }
//
//    @Test
//    void shouldPropagateResourceNotFoundExceptionWhenAlertDoesNotExist() {
//        when(getAlertDetailUseCase.getDetail(alertUuid, caregiverId)).thenThrow(new ResourceNotFoundException("Alerta no encontrada: " + alertUuid));
//
//        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class, () -> riskAlertController.getDetail(alertUuid, caregiverId));
//
//        assertEquals("Alerta no encontrada: " + alertUuid, ex.getMessage());
//        verify(getAlertDetailUseCase, times(1)).getDetail(alertUuid, caregiverId);
//    }
//
//    @Test
//    void shouldPropagateResourceNotFoundExceptionWhenUserIsNotCaregiver() {
//        Long unauthorizedUserId = 99L;
//
//        when(getAlertDetailUseCase.getDetail(alertUuid, unauthorizedUserId)).thenThrow(new ResourceNotFoundException("Alerta no encontrada"));
//
//        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class, () -> riskAlertController.getDetail(alertUuid, unauthorizedUserId));
//
//        assertEquals("Alerta no encontrada", ex.getMessage());
//        verify(getAlertDetailUseCase, times(1)).getDetail(alertUuid, unauthorizedUserId);
//    }
//
//    @Test
//    void shouldPropagateResourceNotFoundExceptionWhenRequestingUserIdIsNull() {
//        when(getAlertDetailUseCase.getDetail(alertUuid, null)).thenThrow(new ResourceNotFoundException("Alerta no encontrada"));
//
//        assertThrows(ResourceNotFoundException.class, () -> riskAlertController.getDetail(alertUuid, null));
//
//        verify(getAlertDetailUseCase, times(1)).getDetail(alertUuid, null);
//    }
// }