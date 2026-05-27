package com.unlam.verabackend.analysis.presentation.controller;

import com.unlam.verabackend.domain.model.RiskAlert;
import com.unlam.verabackend.domain.ports.in.MarkAlertAsReceivedUseCase;
import com.unlam.verabackend.presentation.controller.RiskAlertController;
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
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class RiskAlertControllerTest {

    private MockMvc mockMvc;

    @Mock
    private MarkAlertAsReceivedUseCase markAlertAsReceivedUseCase;

    @BeforeEach
    void setUp() {
        this.mockMvc = MockMvcBuilders.standaloneSetup(new RiskAlertController(markAlertAsReceivedUseCase)).build();
    }

    @Test
    @DisplayName("POST /api/alerts/{id}/received - Debe marcar la alerta como recibida y retornar 200 OK con el DTO")
    void received_WhenAlertExists_ShouldReturnOkAndUpdatedAlert() throws Exception {
        UUID alertId = UUID.randomUUID();
        UUID analysisId = UUID.randomUUID();
        RiskAlert mockAlert = new RiskAlert(alertId, analysisId, 505L, true, LocalDateTime.now());

        when(markAlertAsReceivedUseCase.markAsReceived(alertId)).thenReturn(mockAlert);

        mockMvc.perform(post("/api/alerts/{alertId}/received", alertId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(alertId.toString()))
                .andExpect(jsonPath("$.analysisId").value(analysisId.toString()))
                .andExpect(jsonPath("$.caregiverId").value(505))
                .andExpect(jsonPath("$.received").value(true));
    }

    @Test
    @DisplayName("POST /api/alerts/{id}/received - Debe retornar 404 Not Found si la alerta no existe en la base de datos")
    void received_WhenAlertDoesNotExist_ShouldReturnNotFound() throws Exception {
        UUID nonExistentId = UUID.randomUUID();
        String expectedErrorMessage = "No se encontro la alerta con el ID especificado: " + nonExistentId;

        when(markAlertAsReceivedUseCase.markAsReceived(nonExistentId))
                .thenThrow(new IllegalStateException(expectedErrorMessage));

        mockMvc.perform(post("/api/alerts/{alertId}/received", nonExistentId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().string(expectedErrorMessage));
    }

    @Test
    @DisplayName("POST /api/alerts/{id}/received - Debe retornar 500 Internal Error si salta una excepción genérica")
    void received_WhenUnexpectedError_ShouldReturnInternalServerError() throws Exception {
        UUID alertId = UUID.randomUUID();

        when(markAlertAsReceivedUseCase.markAsReceived(alertId))
                .thenThrow(new RuntimeException("Conexión perdida con el hilo de persistencia"));

        mockMvc.perform(post("/api/alerts/{alertId}/received", alertId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("Error interno al actualizar el estado de la alerta: Conexión perdida con el hilo de persistencia"));
    }
}
