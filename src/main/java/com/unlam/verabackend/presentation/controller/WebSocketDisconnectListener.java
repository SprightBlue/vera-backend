package com.unlam.verabackend.presentation.controller;

import com.unlam.verabackend.domain.model.UserLocation;
import com.unlam.verabackend.domain.port.out.RtcProvider;
import com.unlam.verabackend.domain.port.out.UserLocationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketDisconnectListener {

    private final UserLocationRepository locationRepository;
    private final RtcProvider rtcProvider;

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        Map<String, Object> sessionAttributes = headerAccessor.getSessionAttributes();

        if (sessionAttributes != null && sessionAttributes.containsKey("userEmail")) {
            String email = (String) sessionAttributes.get("userEmail");
            log.info("Presentation Listener: Detectada pérdida de socket o desconexión limpia para el usuario: [{}]", email);

            locationRepository.findByProtectedUserEmail(email).ifPresent(this::processDisconnectionState);
        }
    }

    private void processDisconnectionState(UserLocation userLocation) {
        userLocation.setConnected(false);
        UserLocation savedLocation = locationRepository.save(userLocation);

        if (savedLocation.getTrustContact() != null) {
            Long trackingChannelId = savedLocation.getTrustContact().getId();
            log.debug("Presentation Listener: Sincronizando caída de conexión vía RTC al canal ID [{}]", trackingChannelId);
            rtcProvider.publishLocationUpdate(trackingChannelId, savedLocation);

            if (savedLocation.getTrustContact().getCarer() != null) {
                String carerEmail = savedLocation.getTrustContact().getCarer().getEmail();
                log.info("Presentation Listener: Notificando desconexión al Dashboard global del Carer [{}]", carerEmail);

                rtcProvider.publishCarerDashboardLocationUpdate(carerEmail, savedLocation);
            }
        }
    }
}