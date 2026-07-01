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
    private final String titleSystemPrompt = "Sos un experto en resumen de fraudes. Respondé solo con el título (máx 5 palabras).";

    @BeforeEach
    void setUp() {
        sampleUser = new User();
        sampleUser.setId(1L);
        sampleUser.setEmail("test@unlam.com");
        chatId = UUID.randomUUID();
    }

    @Test
    @DisplayName("Debe crear un chat exitosamente vinculando IDs de análisis y alertas si están presentes")
    void createChat_WithAnalysisAndAlert_ShouldBeSuccessful() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(sampleUser));
        when(chatsRepository.save(any(Chats.class))).thenAnswer(i -> {
            Chats c = i.getArgument(0);
            c.setId(chatId);
            return c;
        });

        UUID result = chatUseCase.createChat("test@unlam.com", UUID.randomUUID(), UUID.randomUUID());

        assertNotNull(result);
        assertEquals(chatId, result);
        verify(chatsRepository, times(1)).save(any(Chats.class));
    }

    @Test
    @DisplayName("Debe lanzar ResourceNotFoundException cuando se intenta crear un chat para un email inexistente")
    void createChat_UserNotFound_ThrowsResourceNotFoundException() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                chatUseCase.createChat("fake@test.com", null, null)
        );
    }

    @Test
    @DisplayName("Debe omitir la generación automática de título si el chat ya no posee el título por defecto")
    void sendMessage_WhenTitleIsNotDefault_ShouldSkipTitleGeneration() {
        Chats chat = Chats.builder().id(chatId).title("Título Personalizado").build();
        when(chatsRepository.findById(chatId)).thenReturn(Optional.of(chat));
        when(promptBuilder.buildChatSystemPrompt(any(), any())).thenReturn("System Prompt");
        when(aiProvider.generateChatResponse(eq("System Prompt"), any())).thenReturn("Respuesta IA");

        String response = chatUseCase.sendMessage(chatId, "Hola VERA");

        assertEquals("Respuesta IA", response);
        verify(promptBuilder, never()).buildTitleGenerationPrompt(anyString());
        verify(aiProvider, never()).generateChatResponse(eq(titleSystemPrompt), anyList());
    }

    @Test
    @DisplayName("Debe procesar el mensaje y actualizar el título limpiando caracteres especiales si el título es el por defecto")
    void sendMessage_SuccessfulFlow_UpdatesEverything() {
        Chats chat = Chats.builder().id(chatId).title("Nueva consulta con VERA").build();
        when(chatsRepository.findById(chatId)).thenReturn(Optional.of(chat));

        when(promptBuilder.buildTitleGenerationPrompt("Hola")).thenReturn("Prompt de Titulo");
        when(aiProvider.generateChatResponse(eq(titleSystemPrompt), anyList())).thenReturn("\"Nuevo Título Simulador\"");

        when(promptBuilder.buildChatSystemPrompt(any(), any())).thenReturn("Prompt del Chat");
        when(aiProvider.generateChatResponse(eq("Prompt del Chat"), any())).thenReturn("Respuesta de la IA");

        String response = chatUseCase.sendMessage(chatId, "Hola");

        assertEquals("Respuesta de la IA", response);
        assertEquals("Nuevo Título Simulador", chat.getTitle());
        assertNotNull(chat.getUpdatedAt());
        verify(chatMessagesRepository, times(1)).save(argThat(msg -> msg.getRole() == ChatsRole.USER));
        verify(chatMessagesRepository, times(1)).save(argThat(msg -> msg.getRole() == ChatsRole.MODEL));
    }

    @Test
    @DisplayName("Debe capturar de forma segura cualquier excepción en la IA al generar títulos y continuar el flujo del chat sin romperlo")
    void sendMessage_WhenTitleGenerationFails_ShouldCatchExceptionAndContinue() {
        Chats chat = Chats.builder().id(chatId).title("Nueva consulta con VERA").build();
        when(chatsRepository.findById(chatId)).thenReturn(Optional.of(chat));

        when(promptBuilder.buildTitleGenerationPrompt("Hola")).thenReturn("Prompt de Titulo");
        when(aiProvider.generateChatResponse(eq(titleSystemPrompt), anyList())).thenThrow(new RuntimeException("IA Saturation"));

        when(promptBuilder.buildChatSystemPrompt(any(), any())).thenReturn("Prompt del Chat");
        when(aiProvider.generateChatResponse(eq("Prompt del Chat"), any())).thenReturn("Respuesta de Emergencia");

        String response = assertDoesNotThrow(() -> chatUseCase.sendMessage(chatId, "Hola"));

        assertEquals("Respuesta de Emergencia", response);
        assertEquals("Nueva consulta con VERA", chat.getTitle()); // Mantiene el título original por el catch
    }

    @Test
    @DisplayName("Debe lanzar IllegalArgumentException al intentar eliminar un chat que no existe")
    void deleteChat_ChatDoesNotExist_ThrowsIllegalArgumentException() {
        when(chatsRepository.findById(chatId)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> chatUseCase.deleteChat(chatId));
    }

    @Test
    @DisplayName("Debe llamar al puerto de eliminación si el chat existe en la base de datos")
    void deleteChat_ChatExists_CallsDelete() {
        when(chatsRepository.findById(chatId)).thenReturn(Optional.of(Chats.builder().id(chatId).build()));

        chatUseCase.deleteChat(chatId);

        verify(chatsRepository, times(1)).deleteById(chatId);
    }

    @Test
    @DisplayName("Debe retornar la lista completa de mensajes asociados al ID de un chat")
    void getChatHistory_ReturnsList() {
        when(chatMessagesRepository.findByChatId(chatId)).thenReturn(Collections.emptyList());

        List<ChatMessages> result = chatUseCase.getChatHistory(chatId);

        assertNotNull(result);
        verify(chatMessagesRepository, times(1)).findByChatId(chatId);
    }

    @Test
    @DisplayName("Debe retornar todos los chats vinculados al correo electrónico de un usuario")
    void getChatsByEmail_ReturnsList() {
        String email = "test@unlam.com";
        when(chatsRepository.findByUserEmail(email)).thenReturn(Collections.emptyList());

        List<Chats> result = chatUseCase.getChatsByEmail(email);

        assertNotNull(result);
        verify(chatsRepository, times(1)).findByUserEmail(email);
    }
}