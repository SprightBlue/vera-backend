package com.unlam.verabackend.domain.port.out;

import com.unlam.verabackend.domain.model.ChatMessages;
import java.util.List;
import java.util.UUID;

public interface ChatMessagesRepository {
    ChatMessages save(ChatMessages message);
    List<ChatMessages> findByChatId(UUID chatId);
    List<ChatMessages> findLastMessages(UUID chatId, int limit);
}