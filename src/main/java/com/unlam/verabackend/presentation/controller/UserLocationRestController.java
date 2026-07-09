package com.unlam.verabackend.presentation.controller;

import com.unlam.verabackend.domain.model.UserLocation;
import com.unlam.verabackend.domain.port.in.GetUserLocationUseCase;
import com.unlam.verabackend.presentation.dto.UserLocationMapResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.security.Principal;

@Slf4j
@RestController
@RequestMapping("/api/protected-people")
@RequiredArgsConstructor
public class UserLocationRestController {

    private final GetUserLocationUseCase getUserLocationUseCase;

    @GetMapping("/{id}/location")
    public ResponseEntity<UserLocationMapResponse> getProtectedUserLocation(@PathVariable Long id, Principal principal) {
        String requesterEmail = principal.getName();
        log.info("REST Request: GET - El operador [{}] solicita la ubicación activa del vínculo ID [{}]", requesterEmail, id);

        UserLocation location = getUserLocationUseCase.execute(id, requesterEmail);

        return ResponseEntity.ok(processLocationResult(location, id));
    }

    private UserLocationMapResponse processLocationResult(UserLocation location, Long trustContactId) {
        if (location == null || location.getTrustContact() == null) {
            log.warn("REST Response: Se validó el acceso a la relación ID [{}], pero carece de telemetría histórica. Construyendo respuesta segura vacía.", trustContactId);
            return buildEmptyLocationResponse(trustContactId);
        }

        log.debug("REST Response: Transmitiendo coordenadas empaquetadas a DTO para mapa interactivo.");
        return UserLocationMapResponse.fromDomain(location);
    }

    private UserLocationMapResponse buildEmptyLocationResponse(Long trustContactId) {
        return UserLocationMapResponse.builder()
                .id(null)
                .trustContactId(trustContactId)
                .latitude(BigDecimal.ZERO)
                .longitude(BigDecimal.ZERO)
                .locationText("Sin registros de ubicación")
                .isConnected(false)
                .updatedAt(null)
                .build();
    }
}