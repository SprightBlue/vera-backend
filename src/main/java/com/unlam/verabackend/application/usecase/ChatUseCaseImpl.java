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
    public UUID createChat(String userEmail, UUID analysisId) {
        log.info("Iniciando creación de chat para el usuario: {} | Análisis de origen ID: {}", userEmail, analysisId);

        var user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> {
                    log.error("Fallo al crear chat: Usuario no encontrado con email: {}", userEmail);
                    return new ResourceNotFoundException("Usuario no encontrado: " + userEmail);
                });

        Chats newChat = Chats.builder()
                .user(user)
                .analysis(analysisId != null ? Analysis.builder().id(analysisId).build() : null)
                .title("Nueva consulta con VERA")
                .build();

        Chats savedChat = chatsRepository.save(newChat);
        log.info("Estructura base del chat persistida exitosamente. Chat ID asignado: {}", savedChat.getId());

        if (analysisId != null) {
            generateInitialAiMessage(savedChat);
        }

        return savedChat.getId();
    }

    private void generateInitialAiMessage(Chats chat) {
        log.info("Generando saludo inicial automatizado con IA para el Chat ID: {}", chat.getId());

        String systemPrompt = promptBuilder.buildChatSystemPrompt(chat.getAnalysis());
        String welcomeInstruction = "Generá un saludo inicial protector y directo para el usuario. " +
                "Explicale brevemente qué encontraste en su análisis y ponete a su disposición para resolver dudas.";

        String aiWelcomeResponse = aiProvider.generateChatResponse(systemPrompt, List.of(
                ChatMessages.builder().role(ChatsRole.USER).content(welcomeInstruction).build()
        ));

        chatMessagesRepository.save(ChatMessages.builder()
                .chat(chat)
                .role(ChatsRole.MODEL)
                .content(aiWelcomeResponse)
                .build());

        log.info("Saludo inicial de VERA persistido correctamente para el Chat ID: {}", chat.getId());
    }

    @Override
    @Transactional
    public String sendMessage(UUID chatId, String userMessage) {
        log.info("Procesando nuevo mensaje del usuario en el Chat ID: {}", chatId);

        if (userMessage == null || userMessage.isBlank()) {
            log.warn("Intento de envío de mensaje rechazado: El contenido está vacío para el Chat ID: {}", chatId);
            throw new IllegalArgumentException("El contenido del mensaje no puede estar vacío.");
        }

        Chats chat = chatsRepository.findById(chatId)
                .orElseThrow(() -> {
                    log.error("Fallo al enviar mensaje: No existe la sala con Chat ID: {}", chatId);
                    return new ResourceNotFoundException("Chat no encontrado: " + chatId);
                });

        saveUserMessage(chat, userMessage);
        updateChatTitleIfNeeded(chat, userMessage);

        return processAiResponse(chat);
    }

    private void saveUserMessage(Chats chat, String content) {
        chatMessagesRepository.save(ChatMessages.builder()
                .chat(chat)
                .role(ChatsRole.USER)
                .content(content.trim())
                .build());
        log.debug("Mensaje del usuario registrado en el historial del Chat ID: {}", chat.getId());
    }

    private void updateChatTitleIfNeeded(Chats chat, String userMessage) {
        if (!"Nueva consulta con VERA".equals(chat.getTitle())) return;

        log.info("Detectado título genérico. Solicitando generación de título contextual para el Chat ID: {}", chat.getId());

        String systemInstruction = "Sos un experto en resumen de fraudes. Respondé solo con el título (máx 5 palabras).";
        String titlePrompt = promptBuilder.buildTitleGenerationPrompt(userMessage);

        String generatedTitle = aiProvider.generateChatResponse(systemInstruction, List.of(
                ChatMessages.builder().role(ChatsRole.USER).content(titlePrompt).build()
        ));

        String cleanTitle = generatedTitle.replaceAll("[\"\n]", "").trim();
        chat.setTitle(cleanTitle);

        chatsRepository.save(chat);
        log.info("Título del Chat ID: {} actualizado dinámicamente a: '{}'", chat.getId(), cleanTitle);
    }

    private String processAiResponse(Chats chat) {
        log.info("Recuperando los últimos mensajes para la ventana de contexto de IA en Chat ID: {}", chat.getId());

        String systemPrompt = promptBuilder.buildChatSystemPrompt(chat.getAnalysis());

        List<ChatMessages> history = chatMessagesRepository.findLastMessages(chat.getId());

        String aiResponse = aiProvider.generateChatResponse(systemPrompt, history);

        chatMessagesRepository.save(ChatMessages.builder()
                .chat(chat)
                .role(ChatsRole.MODEL)
                .content(aiResponse)
                .build());

        log.info("Respuesta de inferencia de VERA persistida exitosamente en Chat ID: {}", chat.getId());
        return aiResponse;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChatMessages> getChatHistory(UUID chatId) {
        log.info("Solicitando historial completo de mensajes para el Chat ID: {}", chatId);

        chatsRepository.findById(chatId)
                .orElseThrow(() -> {
                    log.warn("Intento de lectura de historial fallido: El Chat ID {} no existe", chatId);
                    return new ResourceNotFoundException("Chat no encontrado: " + chatId);
                });

        return chatMessagesRepository.findByChatId(chatId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Chats> getChatsByEmail(String email) {
        log.info("Recuperando listado de chats asociados al email: {}", email);
        return chatsRepository.findByUserEmail(email);
    }

    @Override
    @Transactional
    public void deleteChat(UUID chatId) {
        log.info("Iniciando solicitud de eliminación completa para el Chat ID: {}", chatId);

        chatsRepository.findById(chatId)
                .orElseThrow(() -> {
                    log.warn("Intento de eliminación fallido: El Chat ID {} no existe", chatId);
                    return new ResourceNotFoundException("El chat solicitado no existe o ya fue eliminado.");
                });

        chatsRepository.deleteById(chatId);
        log.info("El Chat ID: {} y todos sus mensajes han sido borrados de la base de datos", chatId);
    }
}