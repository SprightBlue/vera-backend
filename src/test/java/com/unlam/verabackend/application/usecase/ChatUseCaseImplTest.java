package com.unlam.verabackend.application.usecase;

import com.unlam.verabackend.application.service.PromptBuilderService;
import com.unlam.verabackend.domain.exception.ResourceNotFoundException;
import com.unlam.verabackend.domain.model.*;
import com.unlam.verabackend.domain.port.out.ChatMessagesRepository;
import com.unlam.verabackend.domain.port.out.ChatsRepository;
import com.unlam.verabackend.domain.port.out.AiProvider;
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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChatUseCaseImplTest {

    @Mock private AiProvider aiProvider;
    @Mock private ChatsRepository chatsRepository;
    @Mock private ChatMessagesRepository chatMessagesRepository;
    @Mock private PromptBuilderService promptBuilder;
    @Mock private UserRepository userRepository;

    @InjectMocks
    private ChatUseCaseImpl chatUseCase;

    private User sampleUser;
    private UUID chatId;
    private UUID analysisId;
    private final String titleSystemPrompt = "Sos un experto en resumen de fraudes. Respondé solo con el título (máx 5 palabras).";

    @BeforeEach
    void setUp() {
        sampleUser = new User();
        sampleUser.setId(1L);
        sampleUser.setEmail("test@unlam.com");
        chatId = UUID.randomUUID();
        analysisId = UUID.randomUUID();
    }

    @Test
    @DisplayName("Debe crear un chat independiente exitosamente (sin Análisis) y no disparar bienvenida")
    void createChat_WithoutAnalysis_ShouldBeSuccessful() {
        when(userRepository.findByEmail(sampleUser.getEmail())).thenReturn(Optional.of(sampleUser));
        when(chatsRepository.save(any(Chats.class))).thenAnswer(i -> {
            Chats c = i.getArgument(0);
            c.setId(chatId);
            return c;
        });

        UUID result = chatUseCase.createChat(sampleUser.getEmail(), null);

        assertNotNull(result);
        assertEquals(chatId, result);
        verify(chatsRepository, times(1)).save(any(Chats.class));
        verify(chatsRepository, never()).findById(any());
        verify(chatMessagesRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debe crear un chat desde un análisis exitosamente e inyectar el saludo inicial de la IA")
    void createChat_WithAnalysis_ShouldGenerateWelcomeMessage() {
        when(userRepository.findByEmail(sampleUser.getEmail())).thenReturn(Optional.of(sampleUser));
        when(chatsRepository.save(any(Chats.class))).thenAnswer(i -> {
            Chats c = i.getArgument(0);
            c.setId(chatId);
            return c;
        });

        Analysis analysis = Analysis.builder().id(analysisId).riskType(RiskType.CLICKED_SUSPICIOUS_LINK).build();
        Chats hydratedChat = Chats.builder().id(chatId).analysis(analysis).title("Nueva consulta con VERA").build();

        when(chatsRepository.findById(chatId)).thenReturn(Optional.of(hydratedChat));
        when(promptBuilder.buildChatSystemPrompt(analysis)).thenReturn("Prompt de Sistema");
        when(aiProvider.generateChatResponse(eq("Prompt de Sistema"), anyList())).thenReturn("Hola, vi tu análisis...");

        UUID result = chatUseCase.createChat(sampleUser.getEmail(), analysisId);

        assertEquals(chatId, result);
        verify(chatMessagesRepository, times(1)).save(argThat(msg ->
                msg.getRole() == ChatsRole.MODEL && "Hola, vi tu análisis...".equals(msg.getContent())
        ));
    }

    @Test
    @DisplayName("Debe lanzar ResourceNotFoundException si el chat guardado no se encuentra al intentar inicializar el saludo")
    void createChat_HydrationFails_ThrowsResourceNotFoundException() {
        when(userRepository.findByEmail(sampleUser.getEmail())).thenReturn(Optional.of(sampleUser));
        when(chatsRepository.save(any(Chats.class))).thenAnswer(i -> {
            Chats c = i.getArgument(0);
            c.setId(chatId);
            return c;
        });
        when(chatsRepository.findById(chatId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                chatUseCase.createChat(sampleUser.getEmail(), analysisId)
        );
    }

    @Test
    @DisplayName("Debe lanzar ResourceNotFoundException cuando el usuario no existe")
    void createChat_UserNotFound_ThrowsResourceNotFoundException() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                chatUseCase.createChat("fake@test.com", null)
        );
    }

    @Test
    @DisplayName("Debe omitir la generación de título si el chat ya tiene un título personalizado")
    void sendMessage_WhenTitleIsNotDefault_ShouldSkipTitleGeneration() {
        Chats chat = Chats.builder().id(chatId).title("Título Modificado previamente").build();
        when(chatsRepository.findById(chatId)).thenReturn(Optional.of(chat));
        when(promptBuilder.buildChatSystemPrompt(any())).thenReturn("System Prompt");
        when(aiProvider.generateChatResponse(eq("System Prompt"), any())).thenReturn("Respuesta IA");

        String response = chatUseCase.sendMessage(chatId, "Hola VERA");

        assertEquals("Respuesta IA", response);
        verify(promptBuilder, never()).buildTitleGenerationPrompt(anyString());
        verify(aiProvider, never()).generateChatResponse(eq(titleSystemPrompt), anyList());
        verify(chatsRepository, times(1)).save(chat);
    }

    @Test
    @DisplayName("Debe procesar el mensaje, actualizar el título limpiando formatos si posee el string por defecto")
    void sendMessage_WithDefaultTitle_UpdatesTitleAndProcessesMessage() {
        Chats chat = Chats.builder().id(chatId).title("Nueva consulta con VERA").build();
        when(chatsRepository.findById(chatId)).thenReturn(Optional.of(chat));

        when(promptBuilder.buildTitleGenerationPrompt("Ayuda")).thenReturn("Prompt Titulo");
        when(aiProvider.generateChatResponse(eq(titleSystemPrompt), anyList())).thenReturn("\"Nuevo Titulo\"");

        when(promptBuilder.buildChatSystemPrompt(any())).thenReturn("Prompt Chat");
        when(aiProvider.generateChatResponse(eq("Prompt Chat"), any())).thenReturn("Respuesta Core IA");

        String response = chatUseCase.sendMessage(chatId, "Ayuda");

        assertEquals("Respuesta Core IA", response);
        assertEquals("Nuevo Titulo", chat.getTitle());
        verify(chatMessagesRepository, times(1)).save(argThat(msg -> msg.getRole() == ChatsRole.USER && "Ayuda".equals(msg.getContent())));
        verify(chatMessagesRepository, times(1)).save(argThat(msg -> msg.getRole() == ChatsRole.MODEL && "Respuesta Core IA".equals(msg.getContent())));
    }

    @Test
    @DisplayName("Debe lanzar ResourceNotFoundException si el chat destino no existe al enviar un mensaje")
    void sendMessage_ChatNotFound_ThrowsResourceNotFoundException() {
        when(chatsRepository.findById(chatId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                chatUseCase.sendMessage(chatId, "Mensaje")
        );
    }

    @Test
    @DisplayName("Debe lanzar IllegalArgumentException al intentar eliminar un chat inexistente")
    void deleteChat_ChatDoesNotExist_ThrowsIllegalArgumentException() {
        when(chatsRepository.findById(chatId)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> chatUseCase.deleteChat(chatId));
    }

    @Test
    @DisplayName("Debe llamar al puerto de eliminación si el chat existe")
    void deleteChat_ChatExists_CallsDelete() {
        when(chatsRepository.findById(chatId)).thenReturn(Optional.of(Chats.builder().id(chatId).build()));

        chatUseCase.deleteChat(chatId);

        verify(chatsRepository, times(1)).deleteById(chatId);
    }

    @Test
    @DisplayName("Debe retornar la lista de mensajes asociados al ID del chat")
    void getChatHistory_ReturnsList() {
        when(chatMessagesRepository.findByChatId(chatId)).thenReturn(Collections.emptyList());

        List<ChatMessages> result = chatUseCase.getChatHistory(chatId);

        assertNotNull(result);
        verify(chatMessagesRepository, times(1)).findByChatId(chatId);
    }

    @Test
    @DisplayName("Debe retornar todos los chats vinculados al correo del usuario")
    void getChatsByEmail_ReturnsList() {
        String email = "test@unlam.com";
        when(chatsRepository.findByUserEmail(email)).thenReturn(Collections.emptyList());

        List<Chats> result = chatUseCase.getChatsByEmail(email);

        assertNotNull(result);
        verify(chatsRepository, times(1)).findByUserEmail(email);
    }
}