package com.unlam.verabackend.application.usecase;

import com.unlam.verabackend.application.service.PromptBuilderService;
import com.unlam.verabackend.domain.model.*;
import com.unlam.verabackend.domain.port.in.ChatUseCase;
import com.unlam.verabackend.domain.port.out.ChatsRepository;
import com.unlam.verabackend.domain.port.out.ChatMessagesRepository;
import com.unlam.verabackend.domain.port.out.GeminiProvider;
import com.unlam.verabackend.infrastructure.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ChatUseCaseImpl implements ChatUseCase {

    private final GeminiProvider geminiProvider;
    private final ChatsRepository chatsRepository;
    private final ChatMessagesRepository chatMessagesRepository;
    private final PromptBuilderService promptBuilder;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public UUID createChat(String userEmail, UUID analysisId, UUID alertId) {
        var userDomain = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("Usuario de OAuth no encontrado con email: " + userEmail));

        var analysisStub = analysisId != null ? Analysis.builder().id(analysisId).build() : null;
        var alertStub = alertId != null ? Alerts.builder().id(alertId).build() : null;

        Chats newChat = Chats.builder()
                .user(userDomain)
                .analysis(analysisStub)
                .alert(alertStub)
                .title("Nueva consulta con VERA")
                .isActive(true)
                .build();

        Chats savedChat = chatsRepository.save(newChat);
        return savedChat.getId();
    }

    @Override
    @Transactional
    public String sendMessage(UUID chatId, String userMessage) {
        Chats chat = chatsRepository.findById(chatId)
                .orElseThrow(() -> new IllegalArgumentException("El chat solicitado no existe."));

        var analysis = chat.getAnalysis();
        var alert = chat.getAlert();

        String systemPrompt = promptBuilder.buildChatSystemPrompt(analysis, alert);

        ChatMessages userChatMessage = ChatMessages.builder()
                .chat(chat)
                .role(ChatsRole.USER)
                .content(userMessage)
                .build();
        chatMessagesRepository.save(userChatMessage);

        List<ChatMessages> history = chatMessagesRepository.findLastMessages(chatId, 10);

        String aiResponse = geminiProvider.generateChatResponse(systemPrompt, history);

        ChatMessages modelChatMessage = ChatMessages.builder()
                .chat(chat)
                .role(ChatsRole.MODEL)
                .content(aiResponse)
                .build();
        chatMessagesRepository.save(modelChatMessage);

        return aiResponse;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChatMessages> getChatHistory(UUID chatId) {
        return chatMessagesRepository.findByChatId(chatId);
    }
}