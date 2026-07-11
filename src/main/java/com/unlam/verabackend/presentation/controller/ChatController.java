package com.unlam.verabackend.presentation.controller;

import com.unlam.verabackend.domain.port.in.ChatUseCase;
import com.unlam.verabackend.infrastructure.entity.User;
import com.unlam.verabackend.presentation.dto.ChatMessagesResponse;
import com.unlam.verabackend.presentation.dto.ChatSessionResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/chats")
@RequiredArgsConstructor
@Tag(name = "Chats con IA", description = "Endpoints para la gestión de hilos conversacionales y mensajería fluida con la Inteligencia Artificial")
public class ChatController {

    private final ChatUseCase chatUseCase;

    @GetMapping
    @Operation(
            summary = "Listar todas las sesiones de chat del usuario",
            description = "Recupera los encabezados e información de auditoría básica (ID, título, última actualización) de todas las conversaciones activas creadas por el usuario autenticado.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Colección de hilos de chat obtenida con éxito",
                            content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = ChatSessionResponse.class)))
                    ),
                    @ApiResponse(responseCode = "401", description = "No autorizado - Token de sesión inválido", content = @Content)
            }
    )
    public ResponseEntity<List<ChatSessionResponse>> getUserChats(@AuthenticationPrincipal @Parameter(hidden = true) User user) {
        log.info("REST Request: GET - Recopilando canales de chat activos para el operador [{}]", user.getEmail());

        List<ChatSessionResponse> activeChats = chatUseCase.getChatsByEmail(user.getEmail()).stream()
                .map(ChatSessionResponse::fromDomain)
                .toList();

        return ResponseEntity.ok(activeChats);
    }

    @PostMapping("/init")
    @Operation(
            summary = "Inicializar sesión de chat en blanco",
            description = "Crea un nuevo canal o sesión de conversación con la IA desde cero, sin heredar ningún contexto previo de reportes.",
            responses = {
                    @ApiResponse(
                            responseCode = "201",
                            description = "Sesión de chat creada. Retorna el UUID asignado al nuevo canal.",
                            content = @Content(mediaType = "application/json", schema = @Schema(type = "string", format = "uuid", example = "9b1deb4d-3b7d-4bad-9bdd-2b0d7b3dcb6d"))
                    )
            }
    )
    public ResponseEntity<UUID> initializeChat(@AuthenticationPrincipal @Parameter(hidden = true) User user) {
        log.info("REST Request: POST - Inicializando nueva sesión de chat en blanco para [{}]", user.getEmail());

        UUID newChatId = chatUseCase.createChat(user.getEmail(), null);
        return ResponseEntity.status(HttpStatus.CREATED).body(newChatId);
    }

    @PostMapping(value = "/{chatId}/messages", consumes = MediaType.TEXT_PLAIN_VALUE)
    @Operation(
            summary = "Transmitir un mensaje a la IA y obtener respuesta",
            description = "Envía una pregunta o consulta textual dentro de una sesión de chat existente. La API bloquea la conexión hasta retornar la contestación generada por el modelo de IA.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Mensaje procesado. Devuelve la respuesta textual del asistente virtual.",
                            content = @Content(mediaType = "text/plain", schema = @Schema(type = "string", example = "El link recibido contiene indicios claros de phishing bancario. Se sugiere no ingresar credenciales."))
                    ),
                    @ApiResponse(responseCode = "400", description = "Mensaje inválido o cuerpo de petición vacío", content = @Content),
                    @ApiResponse(responseCode = "404", description = "La sesión de chat especificada no existe", content = @Content)
            }
    )
    public ResponseEntity<String> sendMessage(
            @AuthenticationPrincipal @Parameter(hidden = true) User user,
            @PathVariable @Parameter(description = "ID único de la sesión de chat activa", example = "9b1deb4d-3b7d-4bad-9bdd-2b0d7b3dcb6d") UUID chatId,
            @RequestBody @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Contenido en texto plano de la consulta para el asistente", required = true) String message
    ) {
        log.info("REST Request: POST - Transmitiendo mensaje entrante en chat ID [{}] por [{}]", chatId, user.getEmail());
        validateIncomingMessage(message);

        String responseMessage = chatUseCase.sendMessage(chatId, message);
        return ResponseEntity.ok(responseMessage);
    }

    @GetMapping("/{chatId}/messages")
    @Operation(
            summary = "Recuperar el historial secuencial de mensajes",
            description = "Obtiene la secuencia cronológica completa de burbujas de diálogo (mensajes del usuario y respuestas del asistente) pertenecientes a una sesión.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Secuencia histórica mapeada correctamente",
                            content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = ChatMessagesResponse.class)))
                    ),
                    @ApiResponse(responseCode = "404", description = "El ID del chat provisto no existe en los registros", content = @Content)
            }
    )
    public ResponseEntity<List<ChatMessagesResponse>> getChatHistory(
            @AuthenticationPrincipal @Parameter(hidden = true) User user,
            @PathVariable @Parameter(description = "ID del chat del cual se requiere el historial de mensajes", example = "9b1deb4d-3b7d-4bad-9bdd-2b0d7b3dcb6d") UUID chatId
    ) {
        log.info("REST Request: GET - Recuperando secuencia de mensajes para el chat ID [{}] solicitado por [{}]", chatId, user.getEmail());

        List<ChatMessagesResponse> history = chatUseCase.getChatHistory(chatId).stream()
                .map(ChatMessagesResponse::fromDomain)
                .toList();

        return ResponseEntity.ok(history);
    }

    @DeleteMapping("/{chatId}")
    @Operation(
            summary = "Remover una sesión de chat completa",
            description = "Borra físicamente de la persistencia la conversación indicada junto con la totalidad de los mensajes que contenía.",
            responses = {
                    @ApiResponse(responseCode = "204", description = "Canal de chat purgado exitosamente (No Content)", content = @Content),
                    @ApiResponse(responseCode = "404", description = "No se localizó la conversación especificada", content = @Content)
            }
    )
    public ResponseEntity<Void> deleteChat(
            @AuthenticationPrincipal @Parameter(hidden = true) User user,
            @PathVariable @Parameter(description = "ID de la sesión de chat que se desea eliminar permanentemente", example = "9b1deb4d-3b7d-4bad-9bdd-2b0d7b3dcb6d") UUID chatId
    ) {
        log.info("REST Request: DELETE - Solicitando remoción completa del chat ID [{}] por el operador [{}]", chatId, user.getEmail());

        chatUseCase.deleteChat(chatId);
        return ResponseEntity.noContent().build();
    }

    private void validateIncomingMessage(String message) {
        if (message == null || message.isBlank()) {
            log.warn("Payload Validation Exception: Rechazado intento de envío de mensaje con cuerpo vacío.");
            throw new IllegalArgumentException("El contenido del mensaje no puede estar vacío.");
        }
    }
}