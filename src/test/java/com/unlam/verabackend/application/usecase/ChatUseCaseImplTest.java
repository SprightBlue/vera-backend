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
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChatUseCaseImplTest {

    @Mock
    private GeminiProvider geminiProvider;
    @Mock
    private ChatsRepository chatsRepository;
    @Mock
    private ChatMessagesRepository chatMessagesRepository;
    @Mock
    private PromptBuilderService promptBuilder;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ChatUseCaseImpl chatUseCase;

    private User sampleUser;
    private UUID chatId;

    @BeforeEach
    void setUp() {
        sampleUser = new User();
        sampleUser.setId(1L);
        sampleUser.setEmail("test@unlam.com");
        sampleUser.setFullName("Test User");

        chatId = UUID.randomUUID();
    }

    // =========================================================================
    // Pruebas para createChat
    // =========================================================================

    @Test
    void createChat_WhenUserNotFound_ShouldThrowIllegalArgumentException() {
        // Arrange
        String email = "notfound@unlam.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                chatUseCase.createChat(email, null, null)
        );
        assertEquals("Usuario de OAuth no encontrado con email: " + email, exception.getMessage());
        verify(chatsRepository, never()).save(any());
    }

    @Test
    void createChat_WhenAnalysisAndAlertAreProvided_ShouldSaveChatWithStubs() {
        // Arrange
        String email = "test@unlam.com";
        UUID analysisId = UUID.randomUUID();
        UUID alertId = UUID.randomUUID();

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(sampleUser));
        when(chatsRepository.save(any(Chats.class))).thenAnswer(invocation -> {
            Chats chat = invocation.getArgument(0);
            chat.setId(chatId);
            return chat;
        });

        // Act
        UUID resultId = chatUseCase.createChat(email, analysisId, alertId);

        // Assert
        assertEquals(chatId, resultId);
        ArgumentCaptor<Chats> chatCaptor = ArgumentCaptor.forClass(Chats.class);
        verify(chatsRepository).save(chatCaptor.capture());

        Chats savedChat = chatCaptor.getValue();
        assertEquals(sampleUser, savedChat.getUser());
        assertEquals(1L, savedChat.getUser().getId());
        assertNotNull(savedChat.getAnalysis());
        assertEquals(analysisId, savedChat.getAnalysis().getId());
        assertNotNull(savedChat.getAlert());
        assertEquals(alertId, savedChat.getAlert().getId());
        assertTrue(savedChat.isActive());
    }

    @Test
    void createChat_WhenAnalysisAndAlertAreNull_ShouldSaveChatWithNullStubs() {
        // Arrange
        String email = "test@unlam.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(sampleUser));
        when(chatsRepository.save(any(Chats.class))).thenAnswer(invocation -> {
            Chats chat = invocation.getArgument(0);
            chat.setId(chatId);
            return chat;
        });

        // Act
        UUID resultId = chatUseCase.createChat(email, null, null);

        // Assert
        assertEquals(chatId, resultId);
        ArgumentCaptor<Chats> chatCaptor = ArgumentCaptor.forClass(Chats.class);
        verify(chatsRepository).save(chatCaptor.capture());

        Chats savedChat = chatCaptor.getValue();
        assertNull(savedChat.getAnalysis());
        assertNull(savedChat.getAlert());
    }

    // =========================================================================
    // Pruebas para sendMessage
    // =========================================================================

    @Test
    void sendMessage_WhenChatDoesNotExist_ShouldThrowIllegalArgumentException() {
        // Arrange
        UUID nonExistentChatId = UUID.randomUUID();
        when(chatsRepository.findById(nonExistentChatId)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                chatUseCase.sendMessage(nonExistentChatId, "Hola VERA")
        );
        assertEquals("El chat solicitado no existe.", exception.getMessage());
        verify(chatMessagesRepository, never()).save(any());
    }

    @Test
    void sendMessage_WhenChatExists_ShouldSaveMessagesAndReturnAiResponse() {
        // Arrange
        String userMsgText = "Tengo un mail sospechoso";
        String mockAiResponse = "Por seguridad, no abras el enlace.";
        String expectedPrompt = "System Prompt Mocked";

        Analysis analysis = Analysis.builder().id(UUID.randomUUID()).build();
        Alerts alert = Alerts.builder().id(UUID.randomUUID()).build();
        Chats existingChat = Chats.builder().id(chatId).user(sampleUser).analysis(analysis).alert(alert).build();

        List<ChatMessages> mockHistory = List.of(
                ChatMessages.builder().chat(existingChat).role(ChatsRole.USER).content(userMsgText).build()
        );

        when(chatsRepository.findById(chatId)).thenReturn(Optional.of(existingChat));
        when(promptBuilder.buildChatSystemPrompt(analysis, alert)).thenReturn(expectedPrompt);
        when(chatMessagesRepository.findLastMessages(chatId, 10)).thenReturn(mockHistory);
        when(geminiProvider.generateChatResponse(expectedPrompt, mockHistory)).thenReturn(mockAiResponse);

        // Act
        String response = chatUseCase.sendMessage(chatId, userMsgText);

        // Assert
        assertEquals(mockAiResponse, response);

        ArgumentCaptor<ChatMessages> messageCaptor = ArgumentCaptor.forClass(ChatMessages.class);
        verify(chatMessagesRepository, times(2)).save(messageCaptor.capture());

        List<ChatMessages> savedMessages = messageCaptor.getAllValues();

        ChatMessages userMsg = savedMessages.getFirst();
        assertEquals(ChatsRole.USER, userMsg.getRole());
        assertEquals(userMsgText, userMsg.getContent());

        ChatMessages modelMsg = savedMessages.get(1);
        assertEquals(ChatsRole.MODEL, modelMsg.getRole());
        assertEquals(mockAiResponse, modelMsg.getContent());
    }

    // =========================================================================
    // Pruebas para getChatHistory
    // =========================================================================

    @Test
    void getChatHistory_ShouldReturnRepositoryList() {
        // Arrange
        List<ChatMessages> expectedHistory = List.of(
                ChatMessages.builder().role(ChatsRole.USER).content("Hola").build(),
                ChatMessages.builder().role(ChatsRole.MODEL).content("Chau").build()
        );
        when(chatMessagesRepository.findByChatId(chatId)).thenReturn(expectedHistory);

        // Act
        List<ChatMessages> result = chatUseCase.getChatHistory(chatId);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(chatMessagesRepository, times(1)).findByChatId(chatId);
    }
}