package com.unlam.verabackend.application.usecase;

import com.unlam.verabackend.application.service.PromptBuilderService;
import com.unlam.verabackend.domain.exception.ResourceNotFoundException;
import com.unlam.verabackend.domain.model.*;
import com.unlam.verabackend.domain.port.out.AiProvider;
import com.unlam.verabackend.domain.port.out.ChatMessagesRepository;
import com.unlam.verabackend.domain.port.out.ChatsRepository;
import com.unlam.verabackend.infrastructure.entity.User;
import com.unlam.verabackend.infrastructure.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Pruebas Unitarias para ChatUseCaseImpl")
class ChatUseCaseImplTest {

    @Mock private AiProvider aiProvider;
    @Mock private ChatsRepository chatsRepository;
    @Mock private ChatMessagesRepository chatMessagesRepository;
    @Mock private PromptBuilderService promptBuilder;
    @Mock private UserRepository userRepository;

    @InjectMocks private ChatUseCaseImpl chatUseCase;

    private String userEmail;
    private User mockUser;
    private UUID chatId;
    private UUID analysisId;
    private Chats mockChat;

    @BeforeEach
    void setUp() {
        userEmail = "usuario@unlam.edu.ar";
        chatId = UUID.randomUUID();
        analysisId = UUID.randomUUID();

        mockUser = new User();
        mockUser.setId(1L);
        mockUser.setEmail(userEmail);

        mockChat = Chats.builder()
                .id(chatId)
                .user(mockUser)
                .title("Nueva consulta con VERA")
                .build();
    }

    @Test
    @DisplayName("Debería crear un chat básico sin bienvenida automática si no se provee un ID de análisis")
    void createChat_WithoutAnalysis_ShouldCreateSuccessfully() {
        // Arrange
        UUID generatedChatId = UUID.randomUUID();
        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.of(mockUser));

        when(chatsRepository.save(any(Chats.class))).thenAnswer(inv -> {
            Chats chatPassedToSave = inv.getArgument(0);
            chatPassedToSave.setId(generatedChatId);
            return chatPassedToSave;
        });

        // Act
        UUID resultId = chatUseCase.createChat(userEmail, null);

        // Assert
        assertNotNull(resultId);
        assertEquals(generatedChatId, resultId);
        verify(chatsRepository, times(1)).save(any(Chats.class)); // Sigue siendo 1 porque no hay persistMessage
        verify(aiProvider, never()).generateChatResponse(any(), any());
        verify(chatMessagesRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debería crear el chat y generar una bienvenida contextual inicial si se provee un análisis previo")
    void createChat_WithAnalysis_ShouldGenerateWelcomeMessage() {
        // Arrange
        String mockSystemPrompt = "System Prompt Context";
        String mockAiResponse = "Hola, vi que tu análisis posee ciertos riesgos...";

        mockChat.setAnalysis(Analysis.builder().id(analysisId).build());

        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.of(mockUser));
        when(chatsRepository.save(any(Chats.class))).thenReturn(mockChat);
        when(promptBuilder.buildChatSystemPrompt(any())).thenReturn(mockSystemPrompt);
        when(aiProvider.generateChatResponse(eq(mockSystemPrompt), anyList())).thenReturn(mockAiResponse);
        when(chatMessagesRepository.save(any(ChatMessages.class))).thenReturn(new ChatMessages());

        // Act
        UUID resultId = chatUseCase.createChat(userEmail, analysisId);

        // Assert
        assertEquals(chatId, resultId);
        verify(chatsRepository, times(2)).save(any(Chats.class));
        verify(chatMessagesRepository, times(1)).save(any(ChatMessages.class));
    }

    @Test
    @DisplayName("Debería enviar un mensaje, asimilar el contexto histórico y renombrar el título genérico automáticamente")
    void sendMessage_GenricTitle_ShouldUpdateTitleAndReturnAiResponse() {
        // Arrange
        String userMsg = "Me mandaron este link sospechoso";
        String mockTitlePrompt = "Generate Title Prompt";
        String mockRawTitle = "\"Phishing Bancario\"\n";
        String mockSystemPrompt = "System Prompt Context";
        String mockAiResponse = "No entres a ese enlace, es peligroso.";

        when(chatsRepository.findById(chatId)).thenReturn(Optional.of(mockChat));
        when(promptBuilder.buildTitleGenerationPrompt(userMsg.trim())).thenReturn(mockTitlePrompt);
        when(aiProvider.generateChatResponse(eq("Sos un experto en resumen de fraudes. Respondé solo con el título (máx 5 palabras)."), anyList()))
                .thenReturn(mockRawTitle);
        when(promptBuilder.buildChatSystemPrompt(any())).thenReturn(mockSystemPrompt);
        when(chatMessagesRepository.findLastMessages(chatId)).thenReturn(Collections.emptyList());
        when(aiProvider.generateChatResponse(eq(mockSystemPrompt), anyList())).thenReturn(mockAiResponse);

        // Act
        String response = chatUseCase.sendMessage(chatId, userMsg);

        // Assert
        assertEquals(mockAiResponse, response);
        assertEquals("Phishing Bancario", mockChat.getTitle());
        verify(chatsRepository, times(3)).save(mockChat);
        verify(chatMessagesRepository, times(2)).save(any(ChatMessages.class));
    }

    @Test
    @DisplayName("Debería enviar un mensaje sin alterar el título si este ya fue modificado previamente")
    void sendMessage_CustomTitle_ShouldNotTriggerTitleGeneration() {
        // Arrange
        mockChat.setTitle("Título Personalizado");
        String userMsg = "Gracias por el consejo";
        String mockSystemPrompt = "System Prompt Context";
        String mockAiResponse = "De nada, cuidate.";

        when(chatsRepository.findById(chatId)).thenReturn(Optional.of(mockChat));
        when(promptBuilder.buildChatSystemPrompt(any())).thenReturn(mockSystemPrompt);
        when(chatMessagesRepository.findLastMessages(chatId)).thenReturn(Collections.emptyList());
        when(aiProvider.generateChatResponse(eq(mockSystemPrompt), anyList())).thenReturn(mockAiResponse);

        // Act
        String response = chatUseCase.sendMessage(chatId, userMsg);

        // Assert
        assertEquals(mockAiResponse, response);
        assertEquals("Título Personalizado", mockChat.getTitle());
        verify(promptBuilder, never()).buildTitleGenerationPrompt(any());
        verify(chatsRepository, times(2)).save(any(Chats.class));
    }

    @Test
    @DisplayName("Debería retornar la lista completa de mensajes asociados si la sala de chat existe")
    void getChatHistory_ValidScenario_ShouldReturnHistory() {
        // Arrange
        when(chatsRepository.existsById(chatId)).thenReturn(true);
        when(chatMessagesRepository.findByChatId(chatId)).thenReturn(List.of(new ChatMessages()));

        // Act
        List<ChatMessages> result = chatUseCase.getChatHistory(chatId);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("Debería retornar todos los chats asociados al perfil de un usuario")
    void getChatsByEmail_ValidScenario_ShouldReturnCatalog() {
        // Arrange
        when(chatsRepository.findByUserEmail(userEmail)).thenReturn(List.of(mockChat));

        // Act
        List<Chats> result = chatUseCase.getChatsByEmail(userEmail);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("Debería borrar físicamente la sala de chat de la persistencia si existe el ID")
    void deleteChat_ValidScenario_ShouldDelete() {
        // Arrange
        when(chatsRepository.existsById(chatId)).thenReturn(true);
        doNothing().when(chatsRepository).deleteById(chatId);

        // Act
        chatUseCase.deleteChat(chatId);

        // Assert
        verify(chatsRepository, times(1)).deleteById(chatId);
    }

    @Test
    @DisplayName("Debería lanzar ResourceNotFoundException si el usuario creador no está registrado")
    void createChat_UserNotFound_ShouldThrowException() {
        // Arrange
        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> chatUseCase.createChat(userEmail, null));
    }

    @Test
    @DisplayName("Debería lanzar ResourceNotFoundException si se intenta enviar un mensaje a un chat inexistente")
    void sendMessage_ChatNotFound_ShouldThrowException() {
        // Arrange
        when(chatsRepository.findById(chatId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> chatUseCase.sendMessage(chatId, "Hola"));
    }

    @Test
    @DisplayName("Debería lanzar ResourceNotFoundException si se intenta verificar el historial de un chat inexistente")
    void getChatHistory_ChatNotFound_ShouldThrowException() {
        // Arrange
        when(chatsRepository.existsById(chatId)).thenReturn(false);

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> chatUseCase.getChatHistory(chatId));
    }

    @Test
    @DisplayName("Debería retornar título por defecto estructurado ante una respuesta de título nula del backend de IA")
    void sanitizeTitle_NullScenario_ShouldReturnFallback() {
        // Arrange
        when(chatsRepository.findById(chatId)).thenReturn(Optional.of(mockChat));
        when(promptBuilder.buildTitleGenerationPrompt(anyString())).thenReturn("Prompt");
        when(aiProvider.generateChatResponse(anyString(), anyList())).thenReturn(null);
        when(promptBuilder.buildChatSystemPrompt(any())).thenReturn("System");
        when(aiProvider.generateChatResponse(eq("System"), anyList())).thenReturn("Response");

        // Act
        chatUseCase.sendMessage(chatId, "Mensaje");

        // Assert
        assertEquals("Consulta asistida", mockChat.getTitle());
    }
}