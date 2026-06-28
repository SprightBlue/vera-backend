package com.unlam.verabackend.presentation.controller;

import com.unlam.verabackend.domain.model.UserLocation;
import com.unlam.verabackend.domain.port.in.CheckProtectedUserStatusUseCase;
import com.unlam.verabackend.domain.port.in.GetUserLocationUseCase;
import com.unlam.verabackend.presentation.dto.UserLocationResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.Map;

@RestController
@RequestMapping("/api/protected-people")
@RequiredArgsConstructor
@Slf4j
public class UserLocationRestController {

    private final GetUserLocationUseCase getUserLocationUseCase;
    private final CheckProtectedUserStatusUseCase checkProtectedUserStatusUseCase;

    @GetMapping("/{id}/location")
    public ResponseEntity<UserLocationResponseDto> getProtectedUserLocation(@PathVariable Long id, Principal principal) {
        if (principal == null) {
            log.warn("REST Request: Intento de acceso sin autenticación a localización de relación ID [{}]", id);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String requesterEmail = principal.getName();
        log.info("REST Request: El usuario [{}] solicita la ubicación de la relación ID [{}]", requesterEmail, id);

        UserLocation location = getUserLocationUseCase.execute(id, requesterEmail);

        if (location == null || location.getTrustContact() == null) {
            log.warn("REST Response: Se encontró la relación ID [{}] pero no registra posición histórica. Retornando estructura vacía.", id);
            return ResponseEntity.ok(buildEmptyLocationResponse(id));
        }

        return ResponseEntity.ok(UserLocationResponseDto.fromDomain(location));
    }

    @GetMapping("/check-status")
    public ResponseEntity<Map<String, Boolean>> checkProtectedStatus(Principal principal) {
        if (principal == null) {
            log.warn("REST Request: Intento de check-status sin token válido.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String email = principal.getName();
        log.info("REST Request: Comprobando estado de tracking/protección para el usuario: [{}]", email);

        boolean isProtected = checkProtectedUserStatusUseCase.execute(email);

        return ResponseEntity.ok(Map.of("shouldTrackLocation", isProtected));
    }

    private UserLocationResponseDto buildEmptyLocationResponse(Long trustContactId) {
        return UserLocationResponseDto.builder()
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