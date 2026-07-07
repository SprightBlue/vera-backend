package com.unlam.verabackend.application.usecase;

import com.unlam.verabackend.application.service.SseService;
import com.unlam.verabackend.domain.exception.ResourceNotFoundException;
import com.unlam.verabackend.domain.model.Notifications;
import com.unlam.verabackend.domain.port.out.NotificationsRepository;
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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ManageNotificationsUseCaseImplTest {

    @Mock
    private NotificationsRepository repository;

    @Mock
    private SseService sseService;

    @InjectMocks
    private ManageNotificationsUseCaseImpl useCase;

    private String userEmail;
    private UUID notificationId;
    private Notifications mockNotification;

    @BeforeEach
    void setUp() {
        userEmail = "usuario.test@ejemplo.com";
        notificationId = UUID.randomUUID();

        User user = new User();
        user.setEmail(userEmail);

        mockNotification = Notifications.builder()
                .id(notificationId)
                .user(user)
                .build();
    }

    @Test
    @DisplayName("Debe retornar la página de notificaciones ordenada de forma descendente para el usuario solicitado")
    void getMyNotifications_Success() {
        Pageable pageable = PageRequest.of(0, 5);
        Page<Notifications> expectedPage = new PageImpl<>(List.of(mockNotification));
        when(repository.findByUserEmailCreatedAtDesc(userEmail, pageable)).thenReturn(expectedPage);

        Page<Notifications> result = useCase.getMyNotifications(userEmail, pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(repository, times(1)).findByUserEmailCreatedAtDesc(userEmail, pageable);
    }

    @Test
    @DisplayName("Debe delegar el marcado masivo al repositorio y despachar la sincronización de la campana vía SSE")
    void markAllMyNotificationsAsRead_Success() {
        doNothing().when(repository).markAllAsReadByUserEmail(userEmail);
        doNothing().when(sseService).sendUnreadCountUpdate(userEmail);

        useCase.markAllMyNotificationsAsRead(userEmail);

        verify(repository, times(1)).markAllAsReadByUserEmail(userEmail);
        verify(sseService, times(1)).sendUnreadCountUpdate(userEmail); // Verifica la sincronización en tiempo real
    }

    @Test
    @DisplayName("Debe eliminar la notificación del repositorio y emitir el evento NOTIFICATION_DELETED si el usuario es el propietario")
    void deleteNotification_Success() {
        when(repository.findById(notificationId)).thenReturn(Optional.of(mockNotification));
        doNothing().when(repository).deleteById(notificationId);
        doNothing().when(sseService).sendDeleteEvent(userEmail, notificationId);

        useCase.deleteNotification(notificationId, userEmail);

        verify(repository, times(1)).findById(notificationId);
        verify(repository, times(1)).deleteById(notificationId);
        verify(sseService, times(1)).sendDeleteEvent(userEmail, notificationId);
    }

    @Test
    @DisplayName("Debe lanzar ResourceNotFoundException si la notificación no existe en el sistema al intentar eliminarla")
    void deleteNotification_NotFound_ThrowsException() {
        when(repository.findById(notificationId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                useCase.deleteNotification(notificationId, userEmail));

        verify(repository, never()).deleteById(any(UUID.class));
        verify(sseService, never()).sendDeleteEvent(anyString(), any(UUID.class));
    }

    @Test
    @DisplayName("Debe lanzar AccessDeniedException si un usuario malicioso intenta eliminar una notificación que pertenece a otra cuenta")
    void deleteNotification_AccessDenied_ThrowsException() {
        User wrongUser = new User();
        wrongUser.setEmail("otro@ejemplo.com");
        Notifications unauthorizedNotification = Notifications.builder()
                .id(notificationId)
                .user(wrongUser)
                .build();

        when(repository.findById(notificationId)).thenReturn(Optional.of(unauthorizedNotification));

        assertThrows(AccessDeniedException.class, () ->
                useCase.deleteNotification(notificationId, userEmail));

        verify(repository, never()).deleteById(any(UUID.class));
        verify(sseService, never()).sendDeleteEvent(anyString(), any(UUID.class));
    }
}