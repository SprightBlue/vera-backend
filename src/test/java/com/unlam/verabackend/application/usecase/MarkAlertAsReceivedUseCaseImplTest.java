package com.unlam.verabackend.application.usecase;

import com.unlam.verabackend.domain.model.RiskAlert;
import com.unlam.verabackend.domain.ports.out.RiskAlertRepositoryPort;
import com.unlam.verabackend.application.usecase.MarkAlertAsReceivedUseCaseImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MarkAlertAsReceivedUseCaseImplTest {

    @Mock
    private RiskAlertRepositoryPort riskAlertRepositoryPort;

    private MarkAlertAsReceivedUseCaseImpl useCase;

    @BeforeEach
    void setUp() {
        useCase = new MarkAlertAsReceivedUseCaseImpl(riskAlertRepositoryPort);
    }

    @Test
    @DisplayName("Debe marcar la alerta como recibida exitosamente cuando existe el ID")
    void markAsReceived_WhenAlertExists_ShouldUpdateStatusAndSave() {
        UUID alertId = UUID.randomUUID();
        UUID analysisId = UUID.randomUUID();

        RiskAlert mockAlert = new RiskAlert(alertId, analysisId, 200L, false, LocalDateTime.now());

        when(riskAlertRepositoryPort.findById(alertId)).thenReturn(mockAlert);
        when(riskAlertRepositoryPort.save(any(RiskAlert.class))).thenAnswer(invocation -> invocation.getArgument(0));

        RiskAlert result = useCase.markAsReceived(alertId);

        assertNotNull(result);
        assertTrue(result.isReceived(), "La alerta debería haber cambiado su estado a 'true' (recibida)");
        assertEquals(alertId, result.getId());

        verify(riskAlertRepositoryPort, times(1)).findById(alertId);
        verify(riskAlertRepositoryPort, times(1)).save(mockAlert);
    }

    @Test
    @DisplayName("Debe lanzar IllegalArgumentException inmediatamente si el alertId es nulo")
    void markAsReceived_WhenIdIsNull_ShouldThrowIllegalArgumentException() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> useCase.markAsReceived(null));

        assertEquals("El ID de la alerta no puede ser nulo", exception.getMessage());

        verifyNoInteractions(riskAlertRepositoryPort);
    }

    @Test
    @DisplayName("Debe lanzar IllegalStateException si la alerta no es encontrada en el repositorio")
    void markAsReceived_WhenAlertDoesNotExist_ShouldThrowIllegalStateException() {
        UUID nonExistentId = UUID.randomUUID();
        when(riskAlertRepositoryPort.findById(nonExistentId)).thenReturn(null);

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> useCase.markAsReceived(nonExistentId));

        assertTrue(exception.getMessage().contains("No se encontro la alerta con el ID especificado"));

        verify(riskAlertRepositoryPort, times(1)).findById(nonExistentId);
        verify(riskAlertRepositoryPort, never()).save(any(RiskAlert.class));
    }
}
