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

import java.time.LocalDateTime;
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

        ChatMessages userChatMessage = ChatMessages.builder()
                .chat(chat)
                .role(ChatsRole.USER)
                .content(userMessage)
                .build();
        chatMessagesRepository.save(userChatMessage);

        if ("Nueva consulta con VERA".equals(chat.getTitle())) {
            try {
                String titlePrompt = promptBuilder.buildTitleGenerationPrompt(userMessage);
                String titleSystemInstruction = "Sos un algoritmo encargado de resumir consultas de fraude en títulos breves.";

                ChatMessages titleRequestMessage = ChatMessages.builder()
                        .role(ChatsRole.USER)
                        .content(titlePrompt)
                        .build();

                String generatedTitle = geminiProvider.generateChatResponse(titleSystemInstruction, List.of(titleRequestMessage));
                generatedTitle = generatedTitle.replace("\"", "").replace("\n", "").trim();

                if (!generatedTitle.isBlank()) {
                    chat.setTitle(generatedTitle);
                }
            } catch (Exception e) {
                System.err.println("No se pudo actualizar el título dinámico del chat: " + e.getMessage());
            }
        }

        chat.setUpdatedAt(LocalDateTime.now());
        chatsRepository.save(chat);

        List<ChatMessages> history = chatMessagesRepository.findLastMessages(chatId);

        String systemPrompt = promptBuilder.buildChatSystemPrompt(analysis, alert);
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

    @Override
    @Transactional(readOnly = true)
    public List<Chats> getChatsByEmail(String email) {
        return chatsRepository.findByUserEmail(email);
    }

    @Override
    @Transactional
    public void deleteChat(UUID chatId) {
        chatsRepository.findById(chatId)
                .orElseThrow(() -> new IllegalArgumentException("El chat solicitado no existe o ya fue eliminado."));

        chatsRepository.deleteById(chatId);
    }
}