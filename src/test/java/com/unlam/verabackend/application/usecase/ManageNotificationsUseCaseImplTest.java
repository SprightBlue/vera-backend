package com.unlam.verabackend.application.usecase;

import com.unlam.verabackend.domain.exception.ResourceNotFoundException;
import com.unlam.verabackend.domain.model.Notifications;
import com.unlam.verabackend.domain.port.out.NotificationsRepository;
import com.unlam.verabackend.infrastructure.entity.User;
import org.junit.jupiter.api.BeforeEach;
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
    void getMyNotifications_Success() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 5);
        Page<Notifications> expectedPage = new PageImpl<>(List.of(mockNotification));
        when(repository.findByUserEmailCreatedAtDesc(userEmail, pageable)).thenReturn(expectedPage);

        // Act
        Page<Notifications> result = useCase.getMyNotifications(userEmail, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(repository).findByUserEmailCreatedAtDesc(userEmail, pageable);
    }

    @Test
    void markAllMyNotificationsAsRead_Success() {
        // Arrange
        Notifications unreadNotification = Notifications.builder()
                .id(notificationId)
                .user(mockNotification.getUser())
                .isRead(false)
                .build();

        List<Notifications> unreadList = List.of(unreadNotification);

        when(repository.findUnreadByUserEmail(userEmail)).thenReturn(unreadList);

        // Act
        useCase.markAllMyNotificationsAsRead(userEmail);

        // Assert
        assertTrue(unreadNotification.isRead(), "La notificación debería quedar marcada como leída");
        verify(repository).findUnreadByUserEmail(userEmail);
        verify(repository).saveAll(unreadList);
    }

    @Test
    void deleteNotification_Success() {
        // Arrange
        when(repository.findById(notificationId)).thenReturn(Optional.of(mockNotification));

        // Act
        useCase.deleteNotification(notificationId, userEmail);

        // Assert
        verify(repository).deleteById(notificationId);
    }

    @Test
    void deleteNotification_NotFound_ThrowsException() {
        // Arrange
        when(repository.findById(notificationId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () ->
                useCase.deleteNotification(notificationId, userEmail));
    }

    @Test
    void deleteNotification_AccessDenied_ThrowsException() {
        // Arrange
        User wrongUser = new User();
        wrongUser.setEmail("otro@ejemplo.com");
        Notifications unauthorizedNotification = Notifications.builder()
                .id(notificationId)
                .user(wrongUser)
                .build();

        when(repository.findById(notificationId)).thenReturn(Optional.of(unauthorizedNotification));

        // Act & Assert
        assertThrows(AccessDeniedException.class, () ->
                useCase.deleteNotification(notificationId, userEmail));
    }
}