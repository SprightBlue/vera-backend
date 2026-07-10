package com.unlam.verabackend.presentation.controller;

import com.unlam.verabackend.domain.model.Notifications;
import com.unlam.verabackend.domain.port.in.ManageNotificationsUseCase;
import com.unlam.verabackend.infrastructure.entity.User;
import com.unlam.verabackend.presentation.dto.PagedResponse;
import com.unlam.verabackend.presentation.dto.NotificationsResponse;
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
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@Tag(name = "Notificaciones", description = "Endpoints para la gestión, lectura masiva y auditoría del historial de notificaciones automáticas del usuario")
public class NotificationsController {

    private final ManageNotificationsUseCase useCase;

    @GetMapping
    @Operation(
            summary = "Obtener el listado de notificaciones paginado",
            description = "Recupera de forma cronológica e inversa todas las notificaciones (alertas de riesgo, invitaciones, etc.) dirigidas al usuario autenticado.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Centro de notificaciones recuperado exitosamente",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = PagedResponse.class))
                    ),
                    @ApiResponse(responseCode = "401", description = "No autorizado - Token JWT ausente o inválido", content = @Content)
            }
    )
    public ResponseEntity<PagedResponse<NotificationsResponse>> getMyNotifications(
            @AuthenticationPrincipal @Parameter(hidden = true) User user,
            @RequestParam(value = "page", defaultValue = "0") @Parameter(description = "Número de la página a consultar (Basado en índice 0)", example = "0") int page
    ) {
        log.info("REST Request: GET - Consultando catálogo de notificaciones paginadas para el usuario [{}] - Página: [{}]", user.getEmail(), page);

        Page<Notifications> notificationsPage = useCase.getMyNotifications(user.getEmail(), page);

        return ResponseEntity.ok(convertToPagedResponse(notificationsPage));
    }

    @PatchMapping("/read-all")
    @Operation(
            summary = "Marcar todas las notificaciones como leídas",
            description = "Actualiza de manera masiva el estado de todas las notificaciones pendientes del usuario actual, fijando la fecha de lectura al momento de ejecución.",
            responses = {
                    @ApiResponse(responseCode = "204", description = "Todas las notificaciones fueron actualizadas a leídas con éxito", content = @Content)
            }
    )
    public ResponseEntity<Void> markAllAsRead(@AuthenticationPrincipal @Parameter(hidden = true) User user) {
        log.info("REST Request: PATCH - Solicitando marcado de lectura masivo para [{}]", user.getEmail());
        useCase.markAllMyNotificationsAsRead(user.getEmail());
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Eliminar una notificación específica",
            description = "Remueve permanentemente una notificación del buzón personal del usuario utilizando su identificador único.",
            responses = {
                    @ApiResponse(responseCode = "204", description = "Notificación eliminada correctamente de los registros (No Content)", content = @Content),
                    @ApiResponse(responseCode = "403", description = "Acceso denegado - El registro no pertenece al usuario autenticado", content = @Content),
                    @ApiResponse(responseCode = "404", description = "El ID proporcionado no corresponde a ninguna notificación activa", content = @Content)
            }
    )
    public ResponseEntity<Void> deleteNotification(
            @AuthenticationPrincipal @Parameter(hidden = true) User user,
            @PathVariable @Parameter(description = "ID de la notificación que se desea eliminar (UUID)", example = "7b5a1e2f-3d4c-5b6a-7e8f-9a0b1c2d3e4f") UUID id
    ) {
        log.info("REST Request: DELETE - Solicitando remoción de notificación ID [{}] por el operador [{}]", id, user.getEmail());
        useCase.deleteNotification(id, user.getEmail());
        return ResponseEntity.noContent().build();
    }

    private PagedResponse<NotificationsResponse> convertToPagedResponse(Page<Notifications> page) {
        return PagedResponse.fromPage(page, NotificationsResponse::fromDomain);
    }
}