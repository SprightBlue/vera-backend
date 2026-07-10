package com.unlam.verabackend.presentation.controller;

import com.unlam.verabackend.domain.model.UserLocation;
import com.unlam.verabackend.domain.port.in.GetUserLocationUseCase;
import com.unlam.verabackend.presentation.dto.UserLocationResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Geolocalización en Tiempo Real", description = "Endpoints para el rastreo y telemetría de coordenadas geográficas de los usuarios protegidos")
public class UserLocationRestController {

    private final GetUserLocationUseCase getUserLocationUseCase;

    @GetMapping("/{id}/location")
    @Operation(
            summary = "Obtener ubicación en tiempo real de un usuario protegido",
            description = "Recupera las últimas coordenadas geográficas (latitud, longitud y estado de conexión) emitidas por el dispositivo móvil del usuario protegido. Requiere que exista una relación de confianza activa y válida entre el solicitante y el protegido.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Telemetría de ubicación obtenida correctamente (puede retornar un objeto con coordenadas en cero si se validó el acceso pero no registra historial en el dispositivo móvil)",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserLocationResponse.class))
                    ),
                    @ApiResponse(responseCode = "401", description = "No autorizado - Token de sesión ausente o inválido", content = @Content),
                    @ApiResponse(responseCode = "403", description = "Acceso denegado - El solicitante no tiene un vínculo de confianza autorizado con este ID de protegido", content = @Content),
                    @ApiResponse(responseCode = "404", description = "Relación o ID de contacto no encontrado", content = @Content)
            }
    )
    public ResponseEntity<UserLocationResponse> getProtectedUserLocation(
            @PathVariable @Parameter(description = "ID único del contacto de confianza (TrustContact) asignado", example = "1") Long id,
            @Parameter(hidden = true) Principal principal
    ) {
        String requesterEmail = principal.getName();
        log.info("REST Request: GET - El operador [{}] solicita la ubicación activa del vínculo ID [{}]", requesterEmail, id);

        UserLocation location = getUserLocationUseCase.execute(id, requesterEmail);

        return ResponseEntity.ok(processLocationResult(location, id));
    }

    private UserLocationResponse processLocationResult(UserLocation location, Long trustContactId) {
        if (location == null || location.getTrustContact() == null) {
            log.warn("REST Response: Se validó el acceso a la relación ID [{}], pero carece de telemetría histórica. Construyendo respuesta segura vacía.", trustContactId);
            return buildEmptyLocationResponse(trustContactId);
        }

        log.debug("REST Response: Transmitiendo coordenadas empaquetadas a DTO para mapa interactivo.");
        return UserLocationResponse.fromDomain(location);
    }

    private UserLocationResponse buildEmptyLocationResponse(Long trustContactId) {
        return UserLocationResponse.builder()
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