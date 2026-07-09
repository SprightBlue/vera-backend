package com.unlam.verabackend.application.usecase;

import com.unlam.verabackend.application.service.NotificationService;
import com.unlam.verabackend.domain.exception.ResourceNotFoundException;
import com.unlam.verabackend.domain.model.Alerts;
import com.unlam.verabackend.domain.model.NotificationsType;
import com.unlam.verabackend.domain.model.RiskLevel;
import com.unlam.verabackend.domain.port.out.AlertsRepository;
import com.unlam.verabackend.domain.port.out.RtcProvider;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.security.access.AccessDeniedException;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Pruebas Unitarias para ManageAlertsUseCaseImpl")
class ManageAlertsUseCaseImplTest {

    @Mock
    private AlertsRepository alertsRepository;
    @Mock
    private TrustContactRepository trustContactRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private NotificationService notificationService;
    @Mock
    private RtcProvider rtcProvider;

    @InjectMocks
    private ManageAlertsUseCaseImpl manageAlertsUseCase;

    private String carerEmail;
    private String protectedEmail;
    private User mockCarer;
    private TrustContact mockContact;
    private Alerts mockAlert;

    @BeforeEach
    void setUp() {
        carerEmail = "carer@unlam.edu.ar";
        protectedEmail = "protected@unlam.edu.ar";

        mockCarer = new User();
        mockCarer.setId(1L);
        mockCarer.setEmail(carerEmail);
        mockCarer.setFullName("Juan Cuidador");

        User mockProtected = new User();
        mockProtected.setId(2L);
        mockProtected.setEmail(protectedEmail);

        mockContact = new TrustContact();
        mockContact.setId(10L);
        mockContact.setCarer(mockCarer);
        mockContact.setProtectedUser(mockProtected);

        mockAlert = new Alerts();
        mockAlert.setId(UUID.randomUUID());
        mockAlert.setResolved(false);
        mockAlert.setTrustContact(TrustContact.builder()
                .id(10L)
                .carer(User.builder()
                        .id(1L)
                        .email(carerEmail)
                        .fullName("Juan Cuidador")
                        .build())
                .protectedUser(User.builder()
                        .id(2L)
                        .email(protectedEmail)
                        .build())
                .build());
    }

    @Test
    @DisplayName("Debería retornar una página vacía si el cuidador no posee relaciones de confianza registradas")
    void getAlertsHistory_NoContacts_ShouldReturnEmptyPage() {
        // Arrange
        when(userRepository.findByEmail(carerEmail)).thenReturn(Optional.of(mockCarer));
        when(trustContactRepository.findByCarerId(mockCarer.getId())).thenReturn(Collections.emptyList());

        // Act
        Page<Alerts> result = manageAlertsUseCase.getAlertsHistory(carerEmail, false, RiskLevel.HIGH, "", 0);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(alertsRepository, never()).findByCriteria(any(), anyBoolean(), any(), anyString(), anyInt());
    }

    @Test
    @DisplayName("Debería lanzar ResourceNotFoundException si el cuidador no existe al buscar el historial")
    void getAlertsHistory_UserNotFound_ShouldThrowException() {
        // Arrange
        when(userRepository.findByEmail(carerEmail)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () ->
                manageAlertsUseCase.getAlertsHistory(carerEmail, false, RiskLevel.HIGH, "", 0)
        );
    }

    @Test
    @DisplayName("Debería retornar el historial paginado correctamente bajo los criterios establecidos")
    void getAlertsHistory_ValidScenario_ShouldReturnHistoryPage() {
        // Arrange
        Page<Alerts> expectedPage = new PageImpl<>(List.of(mockAlert));
        when(userRepository.findByEmail(carerEmail)).thenReturn(Optional.of(mockCarer));
        when(trustContactRepository.findByCarerId(mockCarer.getId())).thenReturn(List.of(mockContact));
        when(alertsRepository.findByCriteria(List.of(10L), true, RiskLevel.LOW, "pánico", 0))
                .thenReturn(expectedPage);

        // Act
        Page<Alerts> result = manageAlertsUseCase.getAlertsHistory(carerEmail, true, RiskLevel.LOW, "pánico", 0);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(alertsRepository, times(1)).findByCriteria(List.of(10L), true, RiskLevel.LOW, "pánico", 0);
    }

    @Test
    @DisplayName("Debería obtener los detalles de una alerta si pertenece al cuidador solicitante")
    void getAlertDetail_ValidScenario_ShouldReturnAlert() {
        // Arrange
        UUID alertId = mockAlert.getId();
        when(alertsRepository.findById(alertId)).thenReturn(Optional.of(mockAlert));

        // Act
        Alerts result = manageAlertsUseCase.getAlertDetail(alertId, carerEmail);

        // Assert
        assertNotNull(result);
        assertEquals(alertId, result.getId());
    }

    @Test
    @DisplayName("Debería lanzar ResourceNotFoundException si la alerta consultada no existe")
    void validateAndGetOwnedAlert_NotFound_ShouldThrowException() {
        // Arrange
        UUID alertId = UUID.randomUUID();
        when(alertsRepository.findById(alertId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () ->
                manageAlertsUseCase.getAlertDetail(alertId, carerEmail)
        );
    }

    @Test
    @DisplayName("Debería lanzar AccessDeniedException si la alerta pertenece a un vínculo de otro cuidador")
    void validateAndGetOwnedAlert_NotOwner_ShouldThrowAccessDeniedException() {
        // Arrange
        UUID alertId = mockAlert.getId();
        mockAlert.getTrustContact().getCarer().setEmail("stranger@unlam.edu.ar");

        when(alertsRepository.findById(alertId)).thenReturn(Optional.of(mockAlert));

        // Act & Assert
        assertThrows(AccessDeniedException.class, () ->
                manageAlertsUseCase.getAlertDetail(alertId, carerEmail)
        );
    }

    @Test
    @DisplayName("Debería eliminar la alerta correctamente y propagar las bajas vía RTC a ambos dashboards")
    void deleteAlert_ValidScenario_ShouldDelete() {
        // Arrange
        UUID alertId = mockAlert.getId();
        when(alertsRepository.findById(alertId)).thenReturn(Optional.of(mockAlert));
        doNothing().when(alertsRepository).deleteById(alertId);
        doNothing().when(rtcProvider).publishCarerDashboardAlertDeleted(carerEmail, alertId);
        doNothing().when(rtcProvider).publishProtectedDashboardAlertDeleted(protectedEmail, alertId);

        // Act
        manageAlertsUseCase.deleteAlert(alertId, carerEmail);

        // Assert
        verify(alertsRepository, times(1)).deleteById(alertId);
        verify(rtcProvider, times(1)).publishCarerDashboardAlertDeleted(carerEmail, alertId);
        verify(rtcProvider, times(1)).publishProtectedDashboardAlertDeleted(protectedEmail, alertId);
    }

    @Test
    @DisplayName("Debería resolver la alerta, guardar el cambio en cascada y notificar en vivo en el Dashboard del Protegido")
    void resolveAlert_ValidScenario_ShouldResolveAndNotify() {
        // Arrange
        UUID alertId = mockAlert.getId();
        when(alertsRepository.findById(alertId)).thenReturn(Optional.of(mockAlert));
        when(alertsRepository.save(any(Alerts.class), eq(10L))).thenAnswer(invocation -> invocation.getArgument(0));

        doNothing().when(notificationService).createAndDispatch(
                any(),
                eq(NotificationsType.ALERT_SOLVED),
                eq("Juan Cuidador"),
                anyMap()
        );

        doNothing().when(rtcProvider).publishProtectedDashboardResolvedAlertUpdate(eq(protectedEmail), any(Alerts.class));

        // Act
        manageAlertsUseCase.resolveAlert(alertId, carerEmail);

        // Assert
        assertTrue(mockAlert.isResolved(), "La entidad debería marcarse como resuelta en memoria");

        verify(alertsRepository, times(1)).save(any(Alerts.class), eq(10L));
        verify(notificationService, times(1)).createAndDispatch(
                any(),
                eq(NotificationsType.ALERT_SOLVED),
                eq("Juan Cuidador"),
                anyMap()
        );
        verify(rtcProvider, times(1)).publishProtectedDashboardResolvedAlertUpdate(eq(protectedEmail), any(Alerts.class));
        verify(rtcProvider, never()).publishCarerDashboardAlertUpdate(anyString(), any());
    }
}