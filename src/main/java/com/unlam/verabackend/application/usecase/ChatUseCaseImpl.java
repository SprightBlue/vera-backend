package com.unlam.verabackend.application.usecase;

import com.unlam.verabackend.application.service.PromptBuilderService;
import com.unlam.verabackend.domain.exception.ResourceNotFoundException;
import com.unlam.verabackend.domain.model.*;
import com.unlam.verabackend.domain.port.in.ChatUseCase;
import com.unlam.verabackend.domain.port.out.*;
import com.unlam.verabackend.infrastructure.entity.User;
import com.unlam.verabackend.infrastructure.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    private static final String DEFAULT_CHAT_TITLE = "Nueva consulta con VERA";
    private static final String TITLE_SYSTEM_INSTRUCTION = "Sos un experto en resumen de fraudes. Respondé solo con el título (máx 5 palabras).";

    @Override
    @Transactional
    public UUID createChat(String userEmail, UUID analysisId) {
        log.info("UseCase: Creando sala de chat para el usuario [{}] con contexto de análisis [{}]", userEmail, analysisId);

        User user = fetchUserByEmail(userEmail);
        Chats chat = buildBaseChat(user, analysisId);
        Chats savedChat = chatsRepository.save(chat);

        log.debug("UseCase: Base del chat guardada con ID [{}].", savedChat.getId());

        if (analysisId != null) {
            generateInitialAiMessage(savedChat);
        }

        return savedChat.getId();
    }

    @Override
    @Transactional
    public String sendMessage(UUID chatId, String userMessage) {
        log.info("UseCase: Recibido mensaje de usuario en la sala de chat ID [{}]", chatId);

        Chats chat = fetchChatById(chatId);

        persistMessage(chat, ChatsRole.USER, userMessage.trim());
        updateChatTitleIfNeeded(chat, userMessage);

        return requestAndPersistAiInference(chat);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChatMessages> getChatHistory(UUID chatId) {
        log.info("UseCase: Recuperando historial de mensajes para el chat ID [{}]", chatId);
        ensureChatExists(chatId);
        return chatMessagesRepository.findByChatId(chatId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Chats> getChatsByEmail(String email) {
        log.info("UseCase: Buscando catálogo de chats del usuario [{}]", email);
        return chatsRepository.findByUserEmail(email);
    }

    @Override
    @Transactional
    public void deleteChat(UUID chatId) {
        log.info("UseCase: Eliminando de forma permanente el chat ID [{}]", chatId);
        ensureChatExists(chatId);
        chatsRepository.deleteById(chatId);
        log.info("UseCase: Chat ID [{}] removido con éxito.", chatId);
    }

    private User fetchUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.error("UseCase Error: El usuario solicitante [{}] no existe.", email);
                    return new ResourceNotFoundException("Usuario no encontrado: " + email);
                });
    }

    private Chats fetchChatById(UUID chatId) {
        return chatsRepository.findById(chatId)
                .orElseThrow(() -> {
                    log.error("UseCase Error: La sala de chat ID [{}] no existe.", chatId);
                    return new ResourceNotFoundException("Chat no encontrado: " + chatId);
                });
    }

    private void ensureChatExists(UUID chatId) {
        if (!chatsRepository.existsById(chatId)) {
            log.error("UseCase Error: Operación rechazada. El chat ID [{}] no existe.", chatId);
            throw new ResourceNotFoundException("El chat solicitado no existe.");
        }
    }

    private Chats buildBaseChat(User user, UUID analysisId) {
        return Chats.builder()
                .user(user)
                .analysis(analysisId != null ? Analysis.builder().id(analysisId).build() : null)
                .title(DEFAULT_CHAT_TITLE)
                .build();
    }

    private void generateInitialAiMessage(Chats chat) {
        log.info("UseCase: Generando bienvenida heurística automatizada para el chat ID [{}]", chat.getId());

        String systemPrompt = promptBuilder.buildChatSystemPrompt(chat.getAnalysis());
        String instruction = "Generá un saludo inicial protector y directo para el usuario. " +
                "Explicale brevemente qué encontraste en su análisis y ponete a su disposición para resolver dudas.";

        ChatMessages stubMessage = ChatMessages.builder().role(ChatsRole.USER).content(instruction).build();
        String aiResponse = aiProvider.generateChatResponse(systemPrompt, List.of(stubMessage));

        persistMessage(chat, ChatsRole.MODEL, aiResponse);
    }

    private void updateChatTitleIfNeeded(Chats chat, String userMessage) {
        if (!DEFAULT_CHAT_TITLE.equalsIgnoreCase(chat.getTitle())) {
            return;
        }

        log.info("UseCase: Título genérico detectado. Extrayendo síntesis contextual para el chat ID [{}]", chat.getId());
        String titlePrompt = promptBuilder.buildTitleGenerationPrompt(userMessage);

        ChatMessages stubMessage = ChatMessages.builder().role(ChatsRole.USER).content(titlePrompt).build();
        String rawTitle = aiProvider.generateChatResponse(TITLE_SYSTEM_INSTRUCTION, List.of(stubMessage));

        String cleanTitle = sanitizeTitle(rawTitle);
        chat.setTitle(cleanTitle);
        chatsRepository.save(chat);

        log.info("UseCase: Título del chat ID [{}] actualizado a: '{}'", chat.getId(), cleanTitle);
    }

    private String requestAndPersistAiInference(Chats chat) {
        log.debug("UseCase: Extrayendo ventana de contexto histórica para el backend de IA.");
        String systemPrompt = promptBuilder.buildChatSystemPrompt(chat.getAnalysis());
        List<ChatMessages> historicalContext = chatMessagesRepository.findLastMessages(chat.getId());

        String aiResponse = aiProvider.generateChatResponse(systemPrompt, historicalContext);

        persistMessage(chat, ChatsRole.MODEL, aiResponse);
        return aiResponse;
    }

    private void persistMessage(Chats chat, ChatsRole role, String content) {
        ChatMessages message = ChatMessages.builder()
                .chat(chat)
                .role(role)
                .content(content)
                .build();
        chatMessagesRepository.save(message);
        chatsRepository.save(chat);
    }

    private String sanitizeTitle(String rawTitle) {
        if (rawTitle == null) return "Consulta asistida";
        return rawTitle.replaceAll("[\"\n]", "").trim();
    }
}