package com.unlam.verabackend.presentation.controller;

import com.unlam.verabackend.domain.model.UserLocation;
import com.unlam.verabackend.domain.port.out.UserLocationRepository;
import com.unlam.verabackend.presentation.dto.UserLocationResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class WebSocketDisconnectListener {

    private final UserLocationRepository locationRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        try {
            StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
            Map<String, Object> sessionAttributes = headerAccessor.getSessionAttributes();

            if (sessionAttributes != null && sessionAttributes.containsKey("userEmail")) {
                String email = (String) sessionAttributes.get("userEmail");
                log.info("Procesando evento de desconexión STOMP para el usuario: [{}]", email);

                locationRepository.findByProtectedUserEmail(email).ifPresent(userLocation -> {
                    userLocation.setConnected(false);
                    UserLocation savedLocation = locationRepository.save(userLocation);

                    UserLocationResponseDto responseDto = UserLocationResponseDto.fromDomain(savedLocation);

                    String destination = "/topic/trust-contact/" + responseDto.getTrustContactId();
                    messagingTemplate.convertAndSend(destination, responseDto);

                    log.info("Usuario [{}] marcado como desconectado en BD. Notificación enviada al canal: [{}]", email, destination);
                });
            } else {
                log.warn("Se detectó un evento de desconexión de sesión, pero no se encontró la propiedad 'userEmail' en los atributos del socket.");
            }
        } catch (Exception e) {
            log.error("Error crítico al procesar la lógica de desconexión del WebSocket: {}", e.getMessage(), e);
        }
    }
}