package com.unlam.verabackend.infrastructure.provider;

import com.unlam.verabackend.domain.model.Alerts;
import com.unlam.verabackend.domain.model.Notifications;
import com.unlam.verabackend.domain.model.UserLocation;
import com.unlam.verabackend.domain.model.Role;
import com.unlam.verabackend.domain.port.out.RtcProvider;
import com.unlam.verabackend.presentation.dto.AlertsResponse;
import com.unlam.verabackend.presentation.dto.UserLocationMapResponse;
import com.unlam.verabackend.presentation.dto.UserLocationResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketRtcAdapter implements RtcProvider {

    private final SimpMessagingTemplate messagingTemplate;

    @Override
    public void publishLocationUpdate(Long trustContactId, UserLocation location) {
        String destination = "/topic/trust-contact/" + trustContactId;
        UserLocationMapResponse responseDto = UserLocationMapResponse.fromDomain(location);
        log.debug("WS Provider: Transmitiendo coordenadas del PROTECTED al canal del CARER [{}]", destination);
        messagingTemplate.convertAndSend(destination, responseDto);
    }

    @Override
    public void publishNewNotification(String targetEmail, Notifications notification, int unreadCount) {
        String destination = "/topic/users/" + targetEmail + "/notifications";

        log.debug("WS Provider: Despachando evento NEW_NOTIFICATION a [{}]", destination);
        messagingTemplate.convertAndSend(destination, (Object) Map.of(
                "event", "NEW_NOTIFICATION",
                "notification", notification,
                "unreadCount", unreadCount,
                "hasUnread", true
        ));
    }

    @Override
    public void publishNotificationDeleted(String email, UUID notificationId, int unreadCount) {
        String destination = "/topic/users/" + email + "/notifications";

        log.debug("WS Provider: Despachando evento NOTIFICATION_DELETED a [{}]", destination);
        messagingTemplate.convertAndSend(destination, (Object) Map.of(
                "event", "NOTIFICATION_DELETED",
                "id", notificationId,
                "unreadCount", unreadCount,
                "hasUnread", unreadCount > 0
        ));
    }

    @Override
    public void publishUnreadCountUpdate(String email, int unreadCount) {
        String destination = "/topic/users/" + email + "/notifications";

        log.debug("WS Provider: Despachando evento UNREAD_COUNT_UPDATE a [{}]", destination);
        messagingTemplate.convertAndSend(destination, (Object) Map.of(
                "event", "UNREAD_COUNT_UPDATE",
                "unreadCount", unreadCount,
                "hasUnread", unreadCount > 0
        ));
    }

    @Override
    public void publishCarerDashboardLocationUpdate(String carerEmail, UserLocation location) {
        String destination = "/topic/carer/" + carerEmail + "/connected-users";
        UserLocationResponse responseDto = UserLocationResponse.fromDomain(location);
        log.info("WS Provider: Transmitiendo cambio de conexión/red al canal unificado del CARER [{}]", destination);
        messagingTemplate.convertAndSend(destination, responseDto);
    }

    @Override
    public void publishCarerDashboardAlertUpdate(String carerEmail, Alerts alert) {
        String destination = "/topic/carer/" + carerEmail + "/alerts";
        AlertsResponse responseDto = AlertsResponse.fromDomain(alert, Role.CARER);
        log.info("WS Provider: Transmitiendo nueva alerta crítica al canal unificado del CARER [{}]", destination);
        messagingTemplate.convertAndSend(destination, responseDto);
    }

    @Override
    public void publishProtectedDashboardResolvedAlertUpdate(String protectedEmail, Alerts alert) {
        String destination = "/topic/protected/" + protectedEmail + "/resolved-alerts";
        AlertsResponse responseDto = AlertsResponse.fromDomain(alert, Role.PROTECTED);
        log.info("WS Provider: Transmitiendo alerta resuelta al canal unificado del PROTECTED [{}]", destination);
        messagingTemplate.convertAndSend(destination, responseDto);
    }

    @Override
    public void publishCarerDashboardAlertDeleted(String carerEmail, UUID alertId) {
        String destination = "/topic/carer/" + carerEmail + "/alerts";
        log.info("WS Provider: Transmitiendo eliminación de alerta al canal del CARER [{}]", destination);

        messagingTemplate.convertAndSend(destination, (Object) Map.of(
                "event", "ALERT_DELETED",
                "alertId", alertId.toString()
        ));
    }

    @Override
    public void publishProtectedDashboardAlertDeleted(String protectedEmail, UUID alertId) {
        String destination = "/topic/protected/" + protectedEmail + "/resolved-alerts";
        log.info("WS Provider: Transmitiendo eliminación de alerta al canal del PROTECTED [{}]", destination);

        messagingTemplate.convertAndSend(destination, (Object) Map.of(
                "event", "ALERT_DELETED",
                "alertId", alertId.toString()
        ));
    }
}