package com.unlam.verabackend.domain.port.out;

import com.unlam.verabackend.domain.model.Alerts;
import com.unlam.verabackend.domain.model.Notifications;
import com.unlam.verabackend.domain.model.UserLocation;

import java.util.UUID;

public interface RtcProvider {
    void publishLocationUpdate(Long trustContactId, UserLocation location);
    void publishNewNotification(String targetEmail, Notifications notification, int unreadCount);
    void publishNotificationDeleted(String email, UUID notificationId, int unreadCount);
    void publishUnreadCountUpdate(String email, int unreadCount);
    void publishCarerDashboardLocationUpdate(String carerEmail, UserLocation location);
    void publishCarerDashboardAlertUpdate(String carerEmail, Alerts alert);
    void publishProtectedDashboardResolvedAlertUpdate(String protectedEmail, Alerts alert);
    void publishCarerDashboardAlertDeleted(String carerEmail, UUID alertId);
    void publishProtectedDashboardAlertDeleted(String protectedEmail, UUID alertId);
}
