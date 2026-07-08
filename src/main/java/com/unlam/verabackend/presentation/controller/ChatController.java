package com.unlam.verabackend.presentation.controller;

import com.unlam.verabackend.domain.port.in.ChatUseCase;
import com.unlam.verabackend.infrastructure.entity.User;
import com.unlam.verabackend.presentation.dto.ChatMessagesResponse;
import com.unlam.verabackend.presentation.dto.ChatSessionResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/chats")
@RequiredArgsConstructor
public class ChatController {

    private final ChatUseCase chatUseCase;

    @GetMapping
    public ResponseEntity<List<ChatSessionResponse>> getUserChats(@AuthenticationPrincipal User user) {
        log.info("REST Request: GET - Recopilando canales de chat activos para el operador [{}]", user.getEmail());

        List<ChatSessionResponse> activeChats = chatUseCase.getChatsByEmail(user.getEmail()).stream()
                .map(ChatSessionResponse::fromDomain)
                .toList();

        return ResponseEntity.ok(activeChats);
    }

    @PostMapping("/init")
    public ResponseEntity<UUID> initializeChat(@AuthenticationPrincipal User user) {
        log.info("REST Request: POST - Inicializando nueva sesión de chat en blanco para [{}]", user.getEmail());

        UUID newChatId = chatUseCase.createChat(user.getEmail(), null);
        return ResponseEntity.status(HttpStatus.CREATED).body(newChatId);
    }

    @PostMapping("/{chatId}/messages")
    public ResponseEntity<String> sendMessage(
            @AuthenticationPrincipal User user,
            @PathVariable UUID chatId,
            @RequestBody String message
    ) {
        log.info("REST Request: POST - Transmitiendo mensaje entrante en chat ID [{}] por [{}]", chatId, user.getEmail());
        validateIncomingMessage(message);

        String responseMessage = chatUseCase.sendMessage(chatId, message);
        return ResponseEntity.ok(responseMessage);
    }

    @GetMapping("/{chatId}/messages")
    public ResponseEntity<List<ChatMessagesResponse>> getChatHistory(
            @AuthenticationPrincipal User user,
            @PathVariable UUID chatId
    ) {
        log.info("REST Request: GET - Recuperando secuencia de mensajes para el chat ID [{}] solicitado por [{}]", chatId, user.getEmail());

        List<ChatMessagesResponse> history = chatUseCase.getChatHistory(chatId).stream()
                .map(ChatMessagesResponse::fromDomain)
                .toList();

        return ResponseEntity.ok(history);
    }

    @DeleteMapping("/{chatId}")
    public ResponseEntity<Void> deleteChat(@AuthenticationPrincipal User user, @PathVariable UUID chatId) {
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