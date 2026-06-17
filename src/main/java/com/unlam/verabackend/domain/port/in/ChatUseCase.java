package com.unlam.verabackend.domain.port.in;

import com.unlam.verabackend.domain.model.ChatMessages;
import java.util.List;
import java.util.UUID;

public interface ChatUseCase {
    UUID createChat(String userEmail, UUID analysisId, UUID alertId);
    String sendMessage(UUID chatId, String userMessage);
    List<ChatMessages> getChatHistory(UUID chatId);
}