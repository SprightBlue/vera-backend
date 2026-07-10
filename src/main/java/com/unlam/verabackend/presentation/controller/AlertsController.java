package com.unlam.verabackend.presentation.controller;

import com.unlam.verabackend.domain.model.Alerts;
import com.unlam.verabackend.domain.model.RiskLevel;
import com.unlam.verabackend.domain.model.Role;
import com.unlam.verabackend.domain.port.in.ManageAlertsUseCase;
import com.unlam.verabackend.infrastructure.entity.User;
import com.unlam.verabackend.presentation.dto.AlertsDetailResponse;
import com.unlam.verabackend.presentation.dto.AlertsResponse;
import com.unlam.verabackend.presentation.dto.PagedResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/alerts")
@RequiredArgsConstructor
@Tag(name = "Alertas de Seguridad", description = "Endpoints para la auditoría, consulta detallada, descarte y mitigación de alertas críticas dirigidas a los cuidadores")
public class AlertsController {

    private final ManageAlertsUseCase manageAlertsUseCase;

    @GetMapping
    @Operation(
            summary = "Consultar historial de alertas paginado",
            description = "Obtiene la bitácora o lista histórica de alertas de los usuarios vinculados al cuidador autenticado. Soporta filtros opcionales de estado, criticidad y coincidencia por palabras clave.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Página de registros de alertas recuperada con éxito",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = PagedResponse.class))
                    ),
                    @ApiResponse(responseCode = "401", description = "No autorizado - Token JWT inválido o vencido", content = @Content)
            }
    )
    public ResponseEntity<PagedResponse<AlertsResponse>> getAlertsHistory(
            @AuthenticationPrincipal @Parameter(hidden = true) User user,
            @RequestParam(value = "resolved", required = false) @Parameter(description = "Filtro por estado de resolución (true para resueltas, false para activas)", example = "false") Boolean resolved,
            @RequestParam(value = "riskLevel", required = false) @Parameter(description = "Filtro opcional por nivel de severidad", example = "HIGH") RiskLevel riskLevel,
            @RequestParam(value = "search", required = false) @Parameter(description = "Criterio de búsqueda para filtrar por título o contenido de la alerta", example = "María") String search,
            @RequestParam(value = "page", defaultValue = "0") @Parameter(description = "Índice de la página a consultar (Basado en 0)", example = "0") int page
    ) {
        log.info("REST Request: GET - Consultando bitácora de alertas para el cuidador: [{}] con filtros activos", user.getEmail());

        Page<Alerts> alertsPage = manageAlertsUseCase.getAlertsHistory(user.getEmail(), resolved, riskLevel, search, page);

        return ResponseEntity.ok(convertToPagedAlertsResponse(alertsPage));
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Obtener el informe técnico detallado de una alerta",
            description = "Devuelve el desglose forense de una alerta específica (indicadores de compromiso, porcentaje de riesgo exacto y datos del protegido), permitiendo al cuidador evaluar el vector de ataque.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Detalle técnico de la alerta localizado exitosamente",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = AlertsDetailResponse.class))
                    ),
                    @ApiResponse(responseCode = "403", description = "Acceso denegado - El cuidador no tiene permisos sobre el usuario que originó la alerta", content = @Content),
                    @ApiResponse(responseCode = "404", description = "La alerta especificada no existe en la base de datos", content = @Content)
            }
    )
    public ResponseEntity<AlertsDetailResponse> getAlertDetail(
            @AuthenticationPrincipal @Parameter(hidden = true) User user,
            @PathVariable @Parameter(description = "ID único de la alerta (UUID)", example = "4a2b1c3d-5e6f-7a8b-9c0d-1e2f3a4b5c6d") UUID id
    ) {
        log.info("REST Request: GET - Solicitando informe técnico de la alerta ID [{}] por el operador [{}]", id, user.getEmail());

        Alerts alert = manageAlertsUseCase.getAlertDetail(id, user.getEmail());
        return ResponseEntity.ok(AlertsDetailResponse.fromDomain(alert));
    }

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Descarte definitivo de una alerta",
            description = "Remueve físicamente el registro de la alerta seleccionada del sistema.",
            responses = {
                    @ApiResponse(responseCode = "204", description = "Alerta eliminada correctamente (No Content)", content = @Content),
                    @ApiResponse(responseCode = "403", description = "Operación denegada - Privilegios insuficientes", content = @Content),
                    @ApiResponse(responseCode = "404", description = "No se encontró el identificador de alerta provisto", content = @Content)
            }
    )
    public ResponseEntity<Void> deleteAlert(
            @AuthenticationPrincipal @Parameter(hidden = true) User user,
            @PathVariable @Parameter(description = "ID de la alerta a descartar", example = "4a2b1c3d-5e6f-7a8b-9c0d-1e2f3a4b5c6d") UUID id
    ) {
        log.info("REST Request: DELETE - Solicitando descarte definitivo de la alerta ID [{}] por el operador [{}]", id, user.getEmail());

        manageAlertsUseCase.deleteAlert(id, user.getEmail());
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/resolve")
    @Operation(
            summary = "Cerrar y resolver una alerta activa",
            description = "Modifica el estado de la alerta indicando que el cuidador ha tomado acciones preventivas o de mitigación para resguardar al usuario protegido.",
            responses = {
                    @ApiResponse(responseCode = "204", description = "Alerta marcada como resuelta de manera exitosa", content = @Content),
                    @ApiResponse(responseCode = "404", description = "La alerta indicada no existe o ya fue removida", content = @Content)
            }
    )
    public ResponseEntity<Void> resolveAlert(
            @AuthenticationPrincipal @Parameter(hidden = true) User user,
            @PathVariable @Parameter(description = "ID de la alerta que se desea mitigar y archivar", example = "4a2b1c3d-5e6f-7a8b-9c0d-1e2f3a4b5c6d") UUID id
    ) {
        log.info("REST Request: PATCH - Solicitando cierre y resolución de la alerta ID [{}] por el operador [{}]", id, user.getEmail());

        manageAlertsUseCase.resolveAlert(id, user.getEmail());
        return ResponseEntity.noContent().build();
    }

    private PagedResponse<AlertsResponse> convertToPagedAlertsResponse(Page<Alerts> alertsPage) {
        log.debug("REST Response: Convirtiendo {} alertas encontradas hacia el DTO adaptado de la UI.", alertsPage.getNumberOfElements());
        return PagedResponse.fromPage(alertsPage, alert -> AlertsResponse.fromDomain(alert, Role.CARER));
    }
}