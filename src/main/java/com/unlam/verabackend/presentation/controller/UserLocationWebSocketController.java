package com.unlam.verabackend.presentation.controller;

import com.unlam.verabackend.domain.model.UserLocation;
import com.unlam.verabackend.domain.port.in.UpdateUserLocationUseCase;
import com.unlam.verabackend.domain.port.out.RtcProvider;
import com.unlam.verabackend.presentation.dto.LocationUpdateDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.stereotype.Controller;
import java.security.Principal;

@Slf4j
@Controller
@RequiredArgsConstructor
public class UserLocationWebSocketController {

    private final UpdateUserLocationUseCase updateUserLocationUseCase;
    private final RtcProvider rtcProvider;

    @MessageMapping("/location.update")
    public void receiveLocation(LocationUpdateDto dto, Principal principal) {
        String email = principal.getName();
        log.info("STOMP Inbound: Recibida trama de telemetría GPS desde el usuario [{}]", email);

        UserLocation savedLocation = updateUserLocationUseCase.execute(
                email, dto.getLatitude(), dto.getLongitude(), dto.getLocationText()
        );

        dispatchRealTimeUpdate(savedLocation);
    }

    private void dispatchRealTimeUpdate(UserLocation savedLocation) {
        if (savedLocation != null && savedLocation.getTrustContact() != null) {
            Long trackingChannelId = savedLocation.getTrustContact().getId();
            log.debug("STOMP Outbound: Redirigiendo actualización geográfica al canal unificado RTC ID [{}]", trackingChannelId);
            rtcProvider.publishLocationUpdate(trackingChannelId, savedLocation);

            if (savedLocation.getTrustContact().getCarer() != null) {
                String carerEmail = savedLocation.getTrustContact().getCarer().getEmail();
                log.info("STOMP Outbound: Sincronizando posición en vivo hacia el Dashboard global del Carer [{}]", carerEmail);
                rtcProvider.publishCarerDashboardLocationUpdate(carerEmail, savedLocation);
            }
        }
    }

    @MessageExceptionHandler(Exception.class)
    public void handleWebSocketException(Exception ex, Principal principal) {
        String user = (principal != null) ? principal.getName() : "Desconocido";
        log.error("STOMP Exception Handler: Error procesando payload de localización para el usuario [{}]. Detalles: {}", user, ex.getMessage(), ex);
    }
}