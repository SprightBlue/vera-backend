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
        log.info("Usuario {} solicitando su lista de chats", user.getEmail());
        return ResponseEntity.ok(chatUseCase.getChatsByEmail(user.getEmail()).stream()
                .map(ChatSessionResponse::fromDomain)
                .toList());
    }

    @PostMapping("/init")
    public ResponseEntity<UUID> initializeChat(
            @AuthenticationPrincipal User user,
            @RequestParam(required = false) UUID analysisId
    ) {
        log.info("Inicializando chat para usuario: {} desde el análisis: {}", user.getEmail(), analysisId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(chatUseCase.createChat(user.getEmail(), analysisId));
    }

    @PostMapping("/{chatId}/messages")
    public ResponseEntity<String> sendMessage(
            @AuthenticationPrincipal User user,
            @PathVariable UUID chatId,
            @RequestBody String message
    ) {
        validateMessage(message);
        log.info("Usuario {} enviando mensaje en el chat: {}", user.getEmail(), chatId);
        return ResponseEntity.ok(chatUseCase.sendMessage(chatId, message));
    }

    @GetMapping("/{chatId}/messages")
    public ResponseEntity<List<ChatMessagesResponse>> getChatHistory(
            @AuthenticationPrincipal User user,
            @PathVariable UUID chatId
    ) {
        log.info("Usuario {} solicitando historial del chat: {}", user.getEmail(), chatId);
        return ResponseEntity.ok(chatUseCase.getChatHistory(chatId).stream()
                .map(ChatMessagesResponse::fromDomain)
                .toList());
    }

    @DeleteMapping("/{chatId}")
    public ResponseEntity<Void> deleteChat(
            @AuthenticationPrincipal User user,
            @PathVariable UUID chatId
    ) {
        log.info("Usuario {} eliminando el chat: {}", user.getEmail(), chatId);
        chatUseCase.deleteChat(chatId);
        return ResponseEntity.noContent().build();
    }

    private void validateMessage(String message) {
        if (message == null || message.isBlank()) {
            throw new IllegalArgumentException("El contenido del mensaje no puede estar vacío.");
        }
    }
}