package com.unlam.verabackend.domain.port.in;

import com.unlam.verabackend.domain.model.ChatMessages;
import com.unlam.verabackend.domain.model.Chats;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

public interface ChatUseCase {
    @Transactional
    UUID createChat(String userEmail, UUID analysisId);

    @Transactional
    String sendMessage(UUID chatId, String userMessage);

    @Transactional
    List<ChatMessages> getChatHistory(UUID chatId);

    @Transactional
    List<Chats> getChatsByEmail(String email);

    @Transactional
    void deleteChat(UUID chatId);
}