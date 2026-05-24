package com.unlam.verabackend.analysis.presentation.controller;

import com.unlam.verabackend.analysis.domain.model.Analysis;
import com.unlam.verabackend.analysis.domain.model.RiskLevel;
import com.unlam.verabackend.analysis.domain.ports.in.AnalyzeTextUseCase;
import com.unlam.verabackend.analysis.presentation.dto.AnalyzeRequestDto;
import com.unlam.verabackend.analysis.presentation.dto.AnalysisResultDto;
import com.unlam.verabackend.analysis.presentation.dto.ErrorDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AnalyzeControllerTest {

    @Mock
    private AnalyzeTextUseCase analyzeTextUseCase;

    private AnalyzeController controller;

    @BeforeEach
    void setUp() {
        controller = new AnalyzeController(analyzeTextUseCase);
    }

    @Test
    void analyzeSuccessReturnsDto() {
        AnalyzeRequestDto request = new AnalyzeRequestDto();
        UUID messageId = UUID.randomUUID();
        request.setId(messageId);
        request.setUserId(1L);
        request.setContent("Hola, este es un mensaje de prueba.");

        Analysis analysis = new Analysis(
                UUID.randomUUID(),
                messageId,
                false,
                RiskLevel.LOW,
                "Sin patrones",
                "Segui con normalidad",
                LocalDateTime.now()
        );

        when(analyzeTextUseCase.analyzeMessage(any())).thenReturn(analysis);

        ResponseEntity<?> resp = controller.analyze(request);
        assertEquals(200, resp.getStatusCode().value());
        assertInstanceOf(AnalysisResultDto.class, resp.getBody());
        AnalysisResultDto dto = (AnalysisResultDto) resp.getBody();
        assertEquals(analysis.getId(), dto.getId());
        assertEquals(analysis.getMessageId(), dto.getMessageId());
        assertEquals(analysis.isThreat(), dto.isThreat());
    }

    @Test
    void analyzeEmptyMessageReturnsBadRequest() {
        AnalyzeRequestDto request = new AnalyzeRequestDto();
        request.setId(UUID.randomUUID());
        request.setUserId(1L);
        request.setContent("   ");

        when(analyzeTextUseCase.analyzeMessage(any())).thenThrow(new IllegalArgumentException("El mensaje no puede estar vacio"));

        ResponseEntity<?> resp = controller.analyze(request);
        assertEquals(400, resp.getStatusCode().value());
        assertInstanceOf(ErrorDto.class, resp.getBody());
        ErrorDto err = (ErrorDto) resp.getBody();
        assertEquals("El mensaje no puede estar vacio", err.getMessage());
    }

    @Test
    void analyzeFailureReturnsServerError() {
        AnalyzeRequestDto request = new AnalyzeRequestDto();
        request.setId(UUID.randomUUID());
        request.setUserId(1L);
        request.setContent("Hola");

        when(analyzeTextUseCase.analyzeMessage(any())).thenThrow(new RuntimeException("Upstream error"));

        ResponseEntity<?> resp = controller.analyze(request);
        assertEquals(500, resp.getStatusCode().value());
        assertInstanceOf(ErrorDto.class, resp.getBody());
        ErrorDto err = (ErrorDto) resp.getBody();
        assertEquals("Upstream error", err.getMessage());
    }
}
