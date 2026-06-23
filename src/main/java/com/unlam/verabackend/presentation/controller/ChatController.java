package com.unlam.verabackend.presentation.controller;

import com.unlam.verabackend.domain.port.in.ChatUseCase;
import com.unlam.verabackend.infrastructure.entity.User;
import com.unlam.verabackend.presentation.dto.ChatMessagesResponse;
import com.unlam.verabackend.presentation.dto.ChatSessionResponse;
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

    @GetMapping
    public ResponseEntity<List<ChatSessionResponse>> getUserChats(
            @AuthenticationPrincipal User user
    ) {
        String email = user.getEmail();

        List<ChatSessionResponse> response = chatUseCase.getChatsByEmail(email).stream()
                .map(ChatSessionResponse::fromDomain)
                .toList();

        return ResponseEntity.ok(response);
    }

    @PostMapping("/init")
    public ResponseEntity<UUID> initializeChat(
            @AuthenticationPrincipal User user,
            @RequestParam(value = "analysisId", required = false) UUID analysisId,
            @RequestParam(value = "alertId", required = false) UUID alertId
    ) {
        String email = user.getEmail();

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

    @DeleteMapping("/{chatId}")
    public ResponseEntity<Void> deleteChat(
            @PathVariable UUID chatId
    ) {
        chatUseCase.deleteChat(chatId);
        return ResponseEntity.noContent().build();
    }
}