package com.unlam.verabackend.presentation.controller;

import com.unlam.verabackend.domain.port.in.ChatUseCase;
import com.unlam.verabackend.infrastructure.entity.User;
import com.unlam.verabackend.presentation.dto.ChatMessagesResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/chats")
@RequiredArgsConstructor
public class ChatController {

    private final ChatUseCase chatUseCase;

    @PostMapping("/init")
    public ResponseEntity<UUID> initializeChat(
            // @AuthenticationPrincipal User user, // Comentado para testear con cURL sin auth
            @RequestParam(value = "email") String email, // Parámetro temporal de pruebas
            @RequestParam(value = "analysisId", required = false) UUID analysisId,
            @RequestParam(value = "alertId", required = false) UUID alertId
    ) {
        // String email = user.getEmail(); // Comentado para testear con cURL sin auth

        UUID chatId = chatUseCase.createChat(email, analysisId, alertId);
        return new ResponseEntity<>(chatId, HttpStatus.CREATED);
    }

    @PostMapping("/{chatId}/messages")
    public ResponseEntity<String> sendMessage(
            @PathVariable UUID chatId,
            @RequestBody String message
    ) {
        if (message == null || message.isBlank()) {
            return ResponseEntity.badRequest().body("El contenido del mensaje no puede estar vacío.");
        }

        String aiResponse = chatUseCase.sendMessage(chatId, message);
        return ResponseEntity.ok(aiResponse);
    }

    @GetMapping("/{chatId}/messages")
    public ResponseEntity<List<ChatMessagesResponse>> getChatHistory(
            @PathVariable UUID chatId
    ) {
        List<ChatMessagesResponse> response = chatUseCase.getChatHistory(chatId).stream()
                .map(ChatMessagesResponse::fromDomain)
                .toList();

        return ResponseEntity.ok(response);
    }
}