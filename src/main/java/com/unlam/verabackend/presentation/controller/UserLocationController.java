package com.unlam.verabackend.presentation.controller;

import com.unlam.verabackend.domain.port.in.UpdateLocationUseCase;
import com.unlam.verabackend.domain.model.UserLocation;
import com.unlam.verabackend.infrastructure.entity.User;
import com.unlam.verabackend.presentation.dto.LocationRequest;
import com.unlam.verabackend.presentation.dto.UserLocationResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class UserLocationController {

    private final UpdateLocationUseCase updateLocationUseCase;
    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/send-location")
    public void handleLocation(
            @AuthenticationPrincipal User user,
            LocationRequest request
    ) {
        UserLocation updated = updateLocationUseCase.execute(
                user.getEmail(),
                request.getLatitude(),
                request.getLongitude()
        );

        String destination = "/topic/protector/" + updated.getTrustContact().getCarer().getId();
        messagingTemplate.convertAndSend(destination, UserLocationResponse.fromDomain(updated));
    }
}