package com.unlam.verabackend.analysis.presentation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.unlam.verabackend.domain.model.Analysis;
import com.unlam.verabackend.domain.model.Message;
import com.unlam.verabackend.domain.model.RiskLevel;
import com.unlam.verabackend.domain.ports.in.AnalyzeMessageUseCase;
import com.unlam.verabackend.presentation.controller.AnalyzeController;
import com.unlam.verabackend.presentation.dto.MessagePresentation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AnalyzeControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private AnalyzeMessageUseCase analyzeMessageUseCase;

    @BeforeEach
    void setUp() {
        this.mockMvc = MockMvcBuilders.standaloneSetup(new AnalyzeController(analyzeMessageUseCase)).build();
    }

    @Test
    @DisplayName("POST /api/analysis - Debe retornar 200 OK y mapear correctamente la respuesta del análisis")
    void analyze_WhenValidRequest_ShouldReturnOkAndDto() throws Exception {
        MessagePresentation requestDto = new MessagePresentation(10L, "¡Ganaste un premio urgente!", "WHATSAPP");
        UUID mockAnalysisId = UUID.randomUUID();
        UUID mockMessageId = UUID.randomUUID();

        Analysis mockAnalysis = new Analysis(
                mockAnalysisId,
                mockMessageId,
                RiskLevel.HIGH,
                String.valueOf(List.of("Urgencia falsa")),
                "Ignorar el mensaje",
                LocalDateTime.now()
        );

        when(analyzeMessageUseCase.analyzeMessage(any(Message.class))).thenReturn(mockAnalysis);

        mockMvc.perform(post("/api/analysis")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(mockAnalysisId.toString()))
                .andExpect(jsonPath("$.messageId").value(mockMessageId.toString()))
                .andExpect(jsonPath("$.riskLevel").value("Alto"))
                .andExpect(jsonPath("$.recommendation").value("Ignorar el mensaje"));
    }

    @Test
    @DisplayName("POST /api/analysis - Debe retornar 400 Bad Request si el payload es nulo")
    void analyze_WhenRequestBodyIsNull_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(post("/api/analysis")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(""))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/analysis - Debe retornar 400 Bad Request si el caso de uso arroja error de validación")
    void analyze_WhenUseCaseThrowsIllegalArgument_ShouldReturnBadRequest() throws Exception {
        MessagePresentation requestDto = new MessagePresentation(10L, "   ", "WHATSAPP");

        when(analyzeMessageUseCase.analyzeMessage(any(Message.class)))
                .thenThrow(new IllegalArgumentException("El mensaje a analizar no puede ser nulo"));

        mockMvc.perform(post("/api/analysis")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("El mensaje a analizar no puede ser nulo"));
    }

    @Test
    @DisplayName("POST /api/analysis - Debe retornar 500 Internal Server Error si ocurre un fallo no controlado")
    void analyze_WhenUnexpectedException_ShouldReturnInternalServerError() throws Exception {
        MessagePresentation requestDto = new MessagePresentation(10L, "Texto seguro", "TELEGRAM");

        when(analyzeMessageUseCase.analyzeMessage(any(Message.class)))
                .thenThrow(new RuntimeException("Error crítico de timeout externo"));

        mockMvc.perform(post("/api/analysis")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("Ocurrió un error interno al procesar el análisis: Error crítico de timeout externo"));
    }
}
