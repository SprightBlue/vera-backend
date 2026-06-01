package com.unlam.verabackend.application.usecase;

import com.unlam.verabackend.domain.exception.ResourceNotFoundException;
import com.unlam.verabackend.domain.model.*;
import com.unlam.verabackend.domain.ports.out.RiskAlertRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetAlertDetailUseCaseImplTest {

    @Mock
    private RiskAlertRepository riskAlertRepository;

    @InjectMocks
    private GetAlertDetailUseCaseImpl getAlertDetailUseCase;

    private UUID alertId;
    private Long caregiverId;
    private RiskAlert mockAlert;

    @BeforeEach
    void setUp() {
        alertId = UUID.randomUUID();
        caregiverId = 2L;

        DomainUser elderly = new DomainUser(
                1L, "Abuelo Juan", "abuelo@mail.com", Role.ROLE_USER,
                LocalDateTime.now(), LocalDateTime.now(), true, true
        );
        DomainUser caregiver = new DomainUser(
                caregiverId, "Cuidador Pedro", "pedro@mail.com", Role.ROLE_ADMIN,
                LocalDateTime.now(), LocalDateTime.now(), true, true
        );

        Analysis analysis = new Analysis(
                UUID.randomUUID(),
                elderly,
                "Mensaje sospechoso con link",
                MessageSource.WHATSAPP,
                RiskLevel.HIGH,
                "Urgencia falsa",
                "No compartas datos",
                LocalDateTime.now()
        );

        mockAlert = new RiskAlert(alertId, analysis, caregiver, false, LocalDateTime.now());
    }

    @Test
    @DisplayName("Debe devolver AlertDetail cuando el cuidador es el usuario solicitante")
    void shouldReturnAlertDetailWhenCaregiverIsAuthorized() {
        when(riskAlertRepository.findById(alertId.toString())).thenReturn(Optional.of(mockAlert));

        AlertDetail result = getAlertDetailUseCase.getDetail(alertId, caregiverId);

        assertNotNull(result);
        assertEquals(alertId, result.getAlertId());
        assertEquals(mockAlert.getAnalysis().getId(), result.getAnalysisId());
        assertEquals("Mensaje sospechoso con link", result.getMessageContent());
        assertEquals(MessageSource.WHATSAPP, result.getMessageSource());
        assertEquals(RiskLevel.HIGH, result.getRiskLevel());
        assertEquals("Urgencia falsa", result.getSuspiciousPatterns());
        assertEquals("No compartas datos", result.getRecommendation());
        assertFalse(result.isSolved());
        assertEquals(mockAlert.getCreatedAt(), result.getCreatedAt());

        verify(riskAlertRepository, times(1)).findById(alertId.toString());
    }

    @Test
    @DisplayName("Debe lanzar ResourceNotFoundException cuando la alerta no existe")
    void shouldThrowResourceNotFoundExceptionWhenAlertDoesNotExist() {
        when(riskAlertRepository.findById(alertId.toString())).thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class, () ->
                getAlertDetailUseCase.getDetail(alertId, caregiverId)
        );

        assertEquals("Alerta no encontrada: " + alertId, ex.getMessage());
        verify(riskAlertRepository, times(1)).findById(alertId.toString());
    }

    @Test
    @DisplayName("Debe lanzar ResourceNotFoundException genérica cuando el usuario no es el cuidador")
    void shouldThrowResourceNotFoundExceptionWhenUserIsNotCaregiver() {
        when(riskAlertRepository.findById(alertId.toString())).thenReturn(Optional.of(mockAlert));

        Long unauthorizedUserId = 99L;

        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class, () ->
                getAlertDetailUseCase.getDetail(alertId, unauthorizedUserId)
        );

        assertEquals("Alerta no encontrada", ex.getMessage());
        verify(riskAlertRepository, times(1)).findById(alertId.toString());
    }

    @Test
    @DisplayName("Debe mapear el flag solved desde RiskAlert")
    void shouldMapSolvedFlagFromRiskAlert() {
        mockAlert.markAsSolved();
        when(riskAlertRepository.findById(alertId.toString())).thenReturn(Optional.of(mockAlert));

        AlertDetail result = getAlertDetailUseCase.getDetail(alertId, caregiverId);

        assertTrue(result.isSolved());
    }
}