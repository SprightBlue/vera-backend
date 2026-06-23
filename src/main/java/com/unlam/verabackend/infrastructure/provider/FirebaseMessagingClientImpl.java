package com.unlam.verabackend.infrastructure.provider;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.MessagingErrorCode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Component
public class FirebaseMessagingClientImpl implements FirebaseMessagingClient {

    private final String serviceAccountJson;
    private final String serviceAccountPath;
    private volatile FirebaseMessaging firebaseMessaging;
    private volatile boolean initializationAttempted;

    public FirebaseMessagingClientImpl(
            @Value("${firebase.service-account-json:}") String serviceAccountJson,
            @Value("${firebase.service-account-path:}") String serviceAccountPath
    ) {
        this.serviceAccountJson = serviceAccountJson;
        this.serviceAccountPath = serviceAccountPath;
    }

    @Override
    public boolean isConfigured() {
        return resolveMessaging() != null;
    }

    @Override
    public String send(Message message) {
        FirebaseMessaging messaging = resolveMessaging();
        if (messaging == null) {
            return null;
        }

        try {
            return messaging.send(message);
        } catch (FirebaseMessagingException ex) {
            throw new FirebasePushException(
                    ex.getMessage(),
                    isInvalidTokenError(ex.getMessagingErrorCode()),
                    ex
            );
        }
    }

    private FirebaseMessaging resolveMessaging() {
        if (firebaseMessaging != null) {
            return firebaseMessaging;
        }
        if (initializationAttempted) {
            return null;
        }

        synchronized (this) {
            if (firebaseMessaging != null) {
                return firebaseMessaging;
            }
            if (initializationAttempted) {
                return null;
            }

            initializationAttempted = true;

            try {
                if (!FirebaseApp.getApps().isEmpty()) {
                    firebaseMessaging = FirebaseMessaging.getInstance();
                    return firebaseMessaging;
                }

                GoogleCredentials credentials = loadCredentials();
                if (credentials == null) {
                    return null;
                }

                FirebaseOptions options = FirebaseOptions.builder()
                        .setCredentials(credentials)
                        .build();
                FirebaseApp.initializeApp(options);
                firebaseMessaging = FirebaseMessaging.getInstance();
                return firebaseMessaging;
            } catch (IOException | IllegalStateException ex) {
                return null;
            }
        }
    }

    private GoogleCredentials loadCredentials() throws IOException {
        if (serviceAccountJson != null && !serviceAccountJson.isBlank()) {
            try (InputStream stream = new ByteArrayInputStream(serviceAccountJson.getBytes(StandardCharsets.UTF_8))) {
                return GoogleCredentials.fromStream(stream);
            }
        }

        if (serviceAccountPath != null && !serviceAccountPath.isBlank()) {
            try (InputStream stream = new FileInputStream(serviceAccountPath)) {
                return GoogleCredentials.fromStream(stream);
            }
        }

        String applicationCredentials = System.getenv("GOOGLE_APPLICATION_CREDENTIALS");
        if (applicationCredentials != null && !applicationCredentials.isBlank()) {
            return GoogleCredentials.getApplicationDefault();
        }

        return null;
    }

    private boolean isInvalidTokenError(MessagingErrorCode errorCode) {
        return errorCode == MessagingErrorCode.UNREGISTERED
                || errorCode == MessagingErrorCode.INVALID_ARGUMENT;
    }
}
