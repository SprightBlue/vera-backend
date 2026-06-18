package com.unlam.verabackend.application.service;

import com.unlam.verabackend.domain.model.Notifications;
import com.unlam.verabackend.domain.model.NotificationsType;
import com.unlam.verabackend.infrastructure.entity.User;
import com.unlam.verabackend.domain.port.out.NotificationsRepository;
import com.unlam.verabackend.infrastructure.provider.EmailService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationsRepository repository;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private NotificationService notificationService;

    @Test
    void shouldSendEmailWhenNotificationIsAlert() {
        User targetUser = new User();
        targetUser.setEmail("test@test.com");

        Map<String, Object> payload = new HashMap<>();
        payload.put("details", "Detalle de riesgo alto");

        Notifications savedNotification = Notifications.builder().build();
        when(repository.save(any(Notifications.class))).thenReturn(savedNotification);

        notificationService.createAndSendNotification(targetUser, NotificationsType.ALERT, "Juan Perez", payload);

        verify(emailService, times(1)).enviarEmailAlertaRiesgoAlto(eq("test@test.com"), eq("Juan Perez"), eq("Detalle de riesgo alto"));
    }

    @Test
    void shouldNotSendEmailWhenNotificationIsNotAlert() {
        User targetUser = new User();
        targetUser.setEmail("test@test.com");

        Map<String, Object> payload = new HashMap<>();

        Notifications savedNotification = Notifications.builder().build();
        when(repository.save(any(Notifications.class))).thenReturn(savedNotification);

        notificationService.createAndSendNotification(targetUser, NotificationsType.INVITATION, "Juan Perez", payload);

        verify(emailService, never()).enviarEmailAlertaRiesgoAlto(anyString(), anyString(), anyString());
    }
}
