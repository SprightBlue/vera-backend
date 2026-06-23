package com.unlam.verabackend.application.usecase;

import com.unlam.verabackend.application.service.PromptBuilderService;
import com.unlam.verabackend.domain.model.*;
import com.unlam.verabackend.domain.port.out.ChatMessagesRepository;
import com.unlam.verabackend.domain.port.out.ChatsRepository;
import com.unlam.verabackend.domain.port.out.GeminiProvider;
import com.unlam.verabackend.infrastructure.entity.User;
import com.unlam.verabackend.infrastructure.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChatUseCaseImplTest {

    @Mock private GeminiProvider geminiProvider;
    @Mock private ChatsRepository chatsRepository;
    @Mock private ChatMessagesRepository chatMessagesRepository;
    @Mock private PromptBuilderService promptBuilder;
    @Mock private UserRepository userRepository;

    @InjectMocks
    private ChatUseCaseImpl chatUseCase;

    private User sampleUser;
    private UUID chatId;

    @BeforeEach
    void setUp() {
        sampleUser = new User();
        sampleUser.setId(1L);
        sampleUser.setEmail("test@unlam.com");
        chatId = UUID.randomUUID();
    }

    // --- Tests de CreateChat ---

    @Test
    void createChat_WithAnalysisAndAlert_ShouldBeSuccessful() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(sampleUser));
        when(chatsRepository.save(any(Chats.class))).thenAnswer(i -> {
            Chats c = i.getArgument(0);
            c.setId(chatId);
            return c;
        });
        UUID result = chatUseCase.createChat("test@unlam.com", UUID.randomUUID(), UUID.randomUUID());
        assertNotNull(result);
    }

    @Test
    void createChat_UserNotFound_ThrowsException() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> chatUseCase.createChat("fake@test.com", null, null));
    }

    // --- Tests de SendMessage---

    @Test
    void sendMessage_WhenTitleIsNotDefault_ShouldSkipTitleGeneration() {
        Chats chat = Chats.builder().id(chatId).title("Título Personalizado").build();
        when(chatsRepository.findById(chatId)).thenReturn(Optional.of(chat));
        when(promptBuilder.buildChatSystemPrompt(any(), any())).thenReturn("Prompt");
        when(geminiProvider.generateChatResponse(anyString(), any())).thenReturn("Respuesta");

        chatUseCase.sendMessage(chatId, "Hola");

        verify(promptBuilder, never()).buildTitleGenerationPrompt(anyString());
    }

    @Test
    void sendMessage_WhenGeneratedTitleIsBlank_ShouldNotUpdateTitle() {
        Chats chat = Chats.builder().id(chatId).title("Nueva consulta con VERA").build();
        when(chatsRepository.findById(chatId)).thenReturn(Optional.of(chat));
        when(promptBuilder.buildTitleGenerationPrompt(anyString())).thenReturn("Prompt");
        when(geminiProvider.generateChatResponse(contains("algoritmo"), any())).thenReturn("   \n  ");
        when(promptBuilder.buildChatSystemPrompt(any(), any())).thenReturn("Prompt");
        when(geminiProvider.generateChatResponse(eq("Prompt"), any())).thenReturn("Respuesta");

        chatUseCase.sendMessage(chatId, "Hola");

        assertEquals("Nueva consulta con VERA", chat.getTitle());
    }

    @Test
    void sendMessage_SuccessfulFlow_UpdatesEverything() {
        Chats chat = Chats.builder().id(chatId).title("Nueva consulta con VERA").build();
        when(chatsRepository.findById(chatId)).thenReturn(Optional.of(chat));
        when(promptBuilder.buildTitleGenerationPrompt(anyString())).thenReturn("P");
        when(geminiProvider.generateChatResponse(contains("algoritmo"), any())).thenReturn("Nuevo Título");
        when(promptBuilder.buildChatSystemPrompt(any(), any())).thenReturn("P");
        when(geminiProvider.generateChatResponse(eq("P"), any())).thenReturn("Respuesta");

        chatUseCase.sendMessage(chatId, "Hola");

        assertEquals("Nuevo Título", chat.getTitle());
        assertNotNull(chat.getUpdatedAt());
    }

    // --- Tests de eliminación ---

    @Test
    void deleteChat_ChatDoesNotExist_ThrowsException() {
        when(chatsRepository.findById(chatId)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> chatUseCase.deleteChat(chatId));
    }

    @Test
    void deleteChat_ChatExists_CallsDelete() {
        when(chatsRepository.findById(chatId)).thenReturn(Optional.of(Chats.builder().build()));
        chatUseCase.deleteChat(chatId);
        verify(chatsRepository).deleteById(chatId);
    }

    // --- Tests de Listas ---

    @Test
    void getChatHistory_ReturnsList() {
        when(chatMessagesRepository.findByChatId(chatId)).thenReturn(Collections.emptyList());
        assertNotNull(chatUseCase.getChatHistory(chatId));
    }

    @Test
    void getChatsByEmail_ReturnsList() {
        when(chatsRepository.findByUserEmail(anyString())).thenReturn(Collections.emptyList());
        assertNotNull(chatUseCase.getChatsByEmail("test@unlam.com"));
    }
}