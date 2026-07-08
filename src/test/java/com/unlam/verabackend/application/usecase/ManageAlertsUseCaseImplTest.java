package com.unlam.verabackend.application.usecase;

import com.unlam.verabackend.application.service.NotificationService;
import com.unlam.verabackend.domain.exception.ResourceNotFoundException;
import com.unlam.verabackend.domain.model.Alerts;
import com.unlam.verabackend.domain.model.NotificationsType;
import com.unlam.verabackend.domain.model.RiskLevel;
import com.unlam.verabackend.infrastructure.entity.TrustContact;
import com.unlam.verabackend.infrastructure.entity.User;
import com.unlam.verabackend.infrastructure.repository.TrustContactRepository;
import com.unlam.verabackend.infrastructure.repository.UserRepository;
import com.unlam.verabackend.domain.port.out.AlertsRepository;
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

import java.time.LocalDateTime;
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

    @InjectMocks
    private ManageAlertsUseCaseImpl manageAlertsUseCase;

    private String carerEmail;
    private User mockCarer;
    private TrustContact mockContact;
    private Alerts mockAlert;

    @BeforeEach
    void setUp() {
        carerEmail = "carer@unlam.edu.ar";

        mockCarer = new User();
        mockCarer.setId(1L);
        mockCarer.setEmail(carerEmail);
        mockCarer.setFullName("Juan Cuidador");

        User mockProtected = new User();
        mockProtected.setId(2L);
        mockProtected.setEmail("protected@unlam.edu.ar");

        mockContact = new TrustContact();
        mockContact.setId(10L);
        mockContact.setCarer(mockCarer);
        mockContact.setProtectedUser(mockProtected);

        mockAlert = new Alerts();
        mockAlert.setId(UUID.randomUUID());
        mockAlert.setTrustContact(mockContact);
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

        User strangerCarer = new User();
        strangerCarer.setEmail("stranger@unlam.edu.ar");
        mockContact.setCarer(strangerCarer);

        when(alertsRepository.findById(alertId)).thenReturn(Optional.of(mockAlert));

        // Act & Assert
        assertThrows(AccessDeniedException.class, () ->
                manageAlertsUseCase.getAlertDetail(alertId, carerEmail)
        );
    }

    @Test
    @DisplayName("Debería eliminar la alerta correctamente si se cumplen las condiciones de pertenencia")
    void deleteAlert_ValidScenario_ShouldDelete() {
        // Arrange
        UUID alertId = mockAlert.getId();
        when(alertsRepository.findById(alertId)).thenReturn(Optional.of(mockAlert));
        doNothing().when(alertsRepository).deleteById(alertId);

        // Act
        manageAlertsUseCase.deleteAlert(alertId, carerEmail);

        // Assert
        verify(alertsRepository, times(1)).deleteById(alertId);
    }

    @Test
    @DisplayName("Debería resolver la alerta y despachar la notificación correspondiente de cierre por el canal de tiempo real")
    void resolveAlert_ValidScenario_ShouldResolveAndNotify() {
        // Arrange
        UUID alertId = mockAlert.getId();
        when(alertsRepository.findById(alertId)).thenReturn(Optional.of(mockAlert));
        doNothing().when(alertsRepository).resolveAlert(eq(alertId), any(LocalDateTime.class));
        doNothing().when(notificationService).createAndDispatch(
                eq(mockContact.getProtectedUser()),
                eq(NotificationsType.ALERT_SOLVED),
                eq("Juan Cuidador"),
                anyMap()
        );

        // Act
        manageAlertsUseCase.resolveAlert(alertId, carerEmail);

        // Assert
        verify(alertsRepository, times(1)).resolveAlert(eq(alertId), any(LocalDateTime.class));
        verify(notificationService, times(1)).createAndDispatch(
                eq(mockContact.getProtectedUser()),
                eq(NotificationsType.ALERT_SOLVED),
                eq("Juan Cuidador"),
                anyMap()
        );
    }
}