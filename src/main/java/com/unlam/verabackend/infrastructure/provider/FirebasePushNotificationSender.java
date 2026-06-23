package com.unlam.verabackend.infrastructure.provider;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.firebase.messaging.Message;
import com.unlam.verabackend.domain.model.DeviceToken;
import com.unlam.verabackend.domain.model.Notifications;
import com.unlam.verabackend.domain.port.out.DeviceTokenRepository;
import com.unlam.verabackend.domain.port.out.PushNotificationSender;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class FirebasePushNotificationSender implements PushNotificationSender {

    private final DeviceTokenRepository deviceTokenRepository;
    private final FirebaseMessagingClient firebaseMessagingClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void send(Notifications notification) {
        if (notification == null || notification.getUser() == null || !firebaseMessagingClient.isConfigured()) {
            return;
        }

        List<DeviceToken> tokens = deviceTokenRepository.findActiveByUserEmail(notification.getUser().getEmail());
        for (DeviceToken token : tokens) {
            sendToToken(notification, token);
        }
    }

    private void sendToToken(Notifications notification, DeviceToken token) {
        try {
            firebaseMessagingClient.send(Message.builder()
                    .setToken(token.getToken())
                    .putAllData(buildDataPayload(notification))
                    .build());
        } catch (FirebasePushException ex) {
            if (ex.isInvalidToken()) {
                deviceTokenRepository.deactivateToken(token.getToken());
            }
        }
    }

    private Map<String, String> buildDataPayload(Notifications notification) {
        Map<String, String> data = new HashMap<>();
        data.put("event", "NEW_NOTIFICATION");
        data.put("notificationId", String.valueOf(notification.getId()));
        data.put("type", String.valueOf(notification.getType()));
        data.put("title", nullToEmpty(notification.getTitle()));
        data.put("message", nullToEmpty(notification.getMessage()));
        data.put("createdAt", notification.getCreatedAt() == null ? "" : notification.getCreatedAt().toString());
        data.put("payload", toJson(notification.getPayload()));
        return data;
    }

    private String toJson(Map<String, Object> payload) {
        if (payload == null) {
            return "{}";
        }
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException ex) {
            return "{}";
        }
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}
