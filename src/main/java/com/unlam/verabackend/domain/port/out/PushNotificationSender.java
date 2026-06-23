package com.unlam.verabackend.domain.port.out;

import com.unlam.verabackend.domain.model.Notifications;

public interface PushNotificationSender {
    void send(Notifications notification);
}
