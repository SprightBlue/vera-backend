package com.unlam.verabackend.application.usecase;

import com.unlam.verabackend.domain.exception.ResourceNotFoundException;
import com.unlam.verabackend.domain.model.Notifications;
import com.unlam.verabackend.domain.port.out.NotificationsRepository;
import com.unlam.verabackend.domain.port.out.RtcProvider;
import com.unlam.verabackend.infrastructure.entity.User;
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

import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Pruebas Unitarias para ManageNotificationsUseCaseImpl")
class ManageNotificationsUseCaseImplTest {

    @Mock
    private NotificationsRepository repository;

    @Mock
    private RtcProvider rtcProvider;

    @InjectMocks
    private ManageNotificationsUseCaseImpl manageNotificationsUseCase;

    private String userEmail;
    private User validUser;

    @BeforeEach
    void setUp() {
        userEmail = "usuario@unlam.edu.ar";
        validUser = new User();
        validUser.setEmail(userEmail);
    }

    @Test
    @DisplayName("Debería retornar una página de notificaciones delegando el entero de la página al repositorio")
    void getMyNotifications_ValidScenario_ShouldReturnPage() {
        // Arrange
        int page = 0;
        Page<Notifications> expectedPage = new PageImpl<>(Collections.emptyList());

        when(repository.findByUserEmailCreatedAtDesc(userEmail, page)).thenReturn(expectedPage);

        // Act
        Page<Notifications> result = manageNotificationsUseCase.getMyNotifications(userEmail, page);

        // Assert
        assertNotNull(result);
        verify(repository, times(1)).findByUserEmailCreatedAtDesc(userEmail, page);
    }

    @Test
    @DisplayName("Debería marcar todas las notificaciones como leídas y publicar el conteo actualizado por RTC")
    void markAllMyNotificationsAsRead_ValidScenario_ShouldUpdateAndPublish() {
        // Arrange
        doNothing().when(repository).markAllAsReadByUserEmail(userEmail);

        when(repository.countUnreadByUserEmail(userEmail)).thenReturn(0L);
        doNothing().when(rtcProvider).publishUnreadCountUpdate(userEmail, 0);

        // Act
        manageNotificationsUseCase.markAllMyNotificationsAsRead(userEmail);

        // Assert
        verify(repository, times(1)).markAllAsReadByUserEmail(userEmail);
        verify(repository, times(1)).countUnreadByUserEmail(userEmail);
        verify(rtcProvider, times(1)).publishUnreadCountUpdate(userEmail, 0);
    }

    @Test
    @DisplayName("Debería eliminar la notificación exitosamente si el usuario es el propietario y notificar vía RTC")
    void deleteNotification_OwnerScenario_ShouldDeleteAndPublish() {
        // Arrange
        UUID notificationId = UUID.randomUUID();
        Notifications mockNotification = new Notifications();
        mockNotification.setId(notificationId);
        mockNotification.setUser(validUser);

        when(repository.findById(notificationId)).thenReturn(Optional.of(mockNotification));
        doNothing().when(repository).deleteById(notificationId);

        when(repository.countUnreadByUserEmail(userEmail)).thenReturn(3L);
        doNothing().when(rtcProvider).publishNotificationDeleted(userEmail, notificationId, 3);

        // Act
        manageNotificationsUseCase.deleteNotification(notificationId, userEmail);

        // Assert
        verify(repository, times(1)).findById(notificationId);
        verify(repository, times(1)).deleteById(notificationId);
        verify(repository, times(1)).countUnreadByUserEmail(userEmail);
        verify(rtcProvider, times(1)).publishNotificationDeleted(userEmail, notificationId, 3);
    }

    @Test
    @DisplayName("Debería lanzar ResourceNotFoundException si se intenta borrar una notificación inexistente")
    void deleteNotification_NotFound_ShouldThrowResourceNotFoundException() {
        // Arrange
        UUID notificationId = UUID.randomUUID();
        when(repository.findById(notificationId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () ->
                manageNotificationsUseCase.deleteNotification(notificationId, userEmail)
        );

        verify(repository, never()).deleteById(any());
        verify(rtcProvider, never()).publishNotificationDeleted(anyString(), any(), anyInt());
    }

    @Test
    @DisplayName("Debería lanzar AccessDeniedException si un usuario intenta eliminar una notificación ajena")
    void deleteNotification_NotOwner_ShouldThrowAccessDeniedException() {
        // Arrange
        UUID notificationId = UUID.randomUUID();

        User strangerUser = new User();
        strangerUser.setEmail("intruso@unlam.edu.ar");

        Notifications alienNotification = new Notifications();
        alienNotification.setId(notificationId);
        alienNotification.setUser(strangerUser);

        when(repository.findById(notificationId)).thenReturn(Optional.of(alienNotification));

        // Act & Assert
        AccessDeniedException exception = assertThrows(AccessDeniedException.class, () ->
                manageNotificationsUseCase.deleteNotification(notificationId, userEmail)
        );

        assertEquals("No tenés permisos para interactuar con esta notificación.", exception.getMessage());
        verify(repository, never()).deleteById(any());
        verify(rtcProvider, never()).publishNotificationDeleted(anyString(), any(), anyInt());
    }

    @Test
    @DisplayName("Debería eliminar todas las notificaciones del usuario de la persistencia y publicar evento masivo por RTC")
    void deleteAllMyNotifications_ValidScenario_ShouldDeleteAllAndPublish() {
        // Arrange
        doNothing().when(repository).deleteAllByUserEmail(userEmail);
        doNothing().when(rtcProvider).publishAllNotificationsDeleted(userEmail);

        // Act
        assertDoesNotThrow(() -> manageNotificationsUseCase.deleteAllMyNotifications(userEmail));

        // Assert
        verify(repository, times(1)).deleteAllByUserEmail(userEmail);
        verify(rtcProvider, times(1)).publishAllNotificationsDeleted(userEmail);
    }
}