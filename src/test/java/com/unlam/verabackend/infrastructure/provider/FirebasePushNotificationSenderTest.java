package com.unlam.verabackend.infrastructure.provider;

import com.google.firebase.messaging.Message;
import com.unlam.verabackend.domain.model.DeviceToken;
import com.unlam.verabackend.domain.model.Notifications;
import com.unlam.verabackend.domain.model.NotificationsType;
import com.unlam.verabackend.domain.port.out.DeviceTokenRepository;
import com.unlam.verabackend.infrastructure.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.*;

class FirebasePushNotificationSenderTest {

    private DeviceTokenRepository deviceTokenRepository;
    private FirebaseMessagingClient firebaseMessagingClient;
    private FirebasePushNotificationSender sender;
    private User user;
    private Notifications notification;

    @BeforeEach
    void setUp() {
        deviceTokenRepository = mock(DeviceTokenRepository.class);
        firebaseMessagingClient = mock(FirebaseMessagingClient.class);
        sender = new FirebasePushNotificationSender(deviceTokenRepository, firebaseMessagingClient);

        user = new User();
        user.setEmail("test@unlam.edu.ar");

        notification = Notifications.builder()
                .id(UUID.randomUUID())
                .user(user)
                .type(NotificationsType.ALERT)
                .title("Alerta")
                .message("Mensaje")
                .payload(Map.of("details", "Detalle"))
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    void send_WhenNotificationHasActiveTokens_FansOutToFirebase() {
        when(firebaseMessagingClient.isConfigured()).thenReturn(true);
        when(deviceTokenRepository.findActiveByUserEmail(user.getEmail())).thenReturn(List.of(
                DeviceToken.builder().token("token-1").build(),
                DeviceToken.builder().token("token-2").build()
        ));

        sender.send(notification);

        verify(firebaseMessagingClient, times(2)).send(any(Message.class));
    }

    @Test
    void send_WhenFirebaseTokenIsInvalid_DeactivatesToken() {
        when(firebaseMessagingClient.isConfigured()).thenReturn(true);
        when(deviceTokenRepository.findActiveByUserEmail(user.getEmail())).thenReturn(List.of(
                DeviceToken.builder().token("bad-token").build()
        ));
        doThrow(new FirebasePushException("invalid", true, null))
                .when(firebaseMessagingClient).send(any(Message.class));

        assertDoesNotThrow(() -> sender.send(notification));

        verify(deviceTokenRepository).deactivateToken("bad-token");
    }

    @Test
    void send_WhenFirebaseFailsForTransientReason_DoesNotDeactivateToken() {
        when(firebaseMessagingClient.isConfigured()).thenReturn(true);
        when(deviceTokenRepository.findActiveByUserEmail(user.getEmail())).thenReturn(List.of(
                DeviceToken.builder().token("token-1").build()
        ));
        doThrow(new FirebasePushException("temporary", false, null))
                .when(firebaseMessagingClient).send(any(Message.class));

        assertDoesNotThrow(() -> sender.send(notification));

        verify(deviceTokenRepository, never()).deactivateToken(anyString());
    }

    @Test
    void send_WhenFirebaseIsNotConfigured_DoesNothing() {
        when(firebaseMessagingClient.isConfigured()).thenReturn(false);

        sender.send(notification);

        verify(deviceTokenRepository, never()).findActiveByUserEmail(anyString());
        verify(firebaseMessagingClient, never()).send(any(Message.class));
    }

    @Test
    void send_UsesAndroidCompatibleDataPayload() {
        when(firebaseMessagingClient.isConfigured()).thenReturn(true);
        when(deviceTokenRepository.findActiveByUserEmail(user.getEmail())).thenReturn(List.of(
                DeviceToken.builder().token("token-1").build()
        ));

        sender.send(notification);

        ArgumentCaptor<Message> messageCaptor = ArgumentCaptor.forClass(Message.class);
        verify(firebaseMessagingClient).send(messageCaptor.capture());
    }
}
