package com.unlam.verabackend.presentation.controller;

import com.unlam.verabackend.domain.model.UserLocation;
import com.unlam.verabackend.domain.port.in.UpdateUserLocationUseCase;
import com.unlam.verabackend.presentation.dto.LocationUpdateDto;
import com.unlam.verabackend.presentation.dto.UserLocationResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import java.security.Principal;

@Controller
@RequiredArgsConstructor
@Slf4j
public class UserLocationWebSocketController {

    private final UpdateUserLocationUseCase updateUserLocationUseCase;
    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/location.update")
    public void receiveLocation(LocationUpdateDto dto, Principal principal) {
        if (principal == null) {
            log.warn("WebSocket Recv: Intento de actualización de ubicación rechazado debido a Principal nulo (Usuario no autenticado).");
            return;
        }

        String email = principal.getName();
        log.info("WebSocket Recv: Coordenadas STOMP recibidas desde [{}] -> Lat: {}, Lng: {}", email, dto.getLatitude(), dto.getLongitude());

        UserLocation savedLocation = updateUserLocationUseCase.execute(
                email,
                dto.getLatitude(),
                dto.getLongitude(),
                dto.getLocationText()
        );

        UserLocationResponseDto responseDto = UserLocationResponseDto.fromDomain(savedLocation);
        String destination = "/topic/trust-contact/" + responseDto.getTrustContactId();

        log.debug("WebSocket Broadcast: Despachando actualización al canal distributivo [{}]", destination);
        messagingTemplate.convertAndSend(destination, responseDto);
    }


    @MessageExceptionHandler(Exception.class)
    public void handleWebSocketException(Exception ex, Principal principal) {
        String user = (principal != null) ? principal.getName() : "Desconocido";

        log.error("WebSocket Exception Handler: Error procesando trama STOMP para el usuario [{}]. Detalles: {}", user, ex.getMessage(), ex);
    }
}