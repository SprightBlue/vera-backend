package com.unlam.verabackend.infrastructure.provider;

import com.google.firebase.messaging.Message;

public interface FirebaseMessagingClient {
    boolean isConfigured();
    String send(Message message);
}
