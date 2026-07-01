package com.unlam.verabackend.application.usecase;

import com.unlam.verabackend.application.service.PromptBuilderService;
import com.unlam.verabackend.domain.exception.ResourceNotFoundException;
import com.unlam.verabackend.domain.model.*;
import com.unlam.verabackend.domain.port.in.ChatUseCase;
import com.unlam.verabackend.domain.port.out.*;
import com.unlam.verabackend.infrastructure.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatUseCaseImpl implements ChatUseCase {

    private final AiProvider aiProvider;
    private final ChatsRepository chatsRepository;
    private final ChatMessagesRepository chatMessagesRepository;
    private final PromptBuilderService promptBuilder;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public UUID createChat(String userEmail, UUID analysisId, UUID alertId) {
        log.info("Creando nuevo chat para el usuario: {}", userEmail);
        var user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + userEmail));

        Chats newChat = Chats.builder()
                .user(user)
                .analysis(analysisId != null ? Analysis.builder().id(analysisId).build() : null)
                .alert(alertId != null ? Alerts.builder().id(alertId).build() : null)
                .title("Nueva consulta con VERA")
                .build();

        return chatsRepository.save(newChat).getId();
    }

    @Override
    @Transactional
    public String sendMessage(UUID chatId, String userMessage) {
        log.info("Procesando mensaje en chat: {}", chatId);
        Chats chat = chatsRepository.findById(chatId)
                .orElseThrow(() -> new ResourceNotFoundException("Chat no encontrado: " + chatId));

        saveUserMessage(chat, userMessage);
        updateChatTitleIfNeeded(chat, userMessage);

        chat.setUpdatedAt(LocalDateTime.now());
        chatsRepository.save(chat);

        return processAiResponse(chat);
    }

    private void saveUserMessage(Chats chat, String content) {
        chatMessagesRepository.save(ChatMessages.builder()
                .chat(chat).role(ChatsRole.USER).content(content).build());
    }

    private void updateChatTitleIfNeeded(Chats chat, String userMessage) {
        if (!"Nueva consulta con VERA".equals(chat.getTitle())) return;

        try {
            String title = aiProvider.generateChatResponse("Sos un experto en resumen de fraudes. Respondé solo con el título (máx 5 palabras).",
                    List.of(ChatMessages.builder().role(ChatsRole.USER).content(promptBuilder.buildTitleGenerationPrompt(userMessage)).build()));
            chat.setTitle(title.replaceAll("[\"\n]", "").trim());
        } catch (Exception e) {
            log.warn("No se pudo actualizar el título del chat {}: {}", chat.getId(), e.getMessage());
        }
    }

    private String processAiResponse(Chats chat) {
        String systemPrompt = promptBuilder.buildChatSystemPrompt(chat.getAnalysis(), chat.getAlert());
        List<ChatMessages> history = chatMessagesRepository.findLastMessages(chat.getId());

        String aiResponse = aiProvider.generateChatResponse(systemPrompt, history);

        chatMessagesRepository.save(ChatMessages.builder()
                .chat(chat).role(ChatsRole.MODEL).content(aiResponse).build());

        log.info("Respuesta de IA generada para el chat: {}", chat.getId());
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

        log.info("Eliminando chat: {}", chatId);
        chatsRepository.deleteById(chatId);
    }
}