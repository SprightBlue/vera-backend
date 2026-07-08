package com.unlam.verabackend.application.usecase;

import com.unlam.verabackend.domain.exception.ResourceNotFoundException;
import com.unlam.verabackend.domain.model.Analysis;
import com.unlam.verabackend.domain.model.RiskLevel;
import com.unlam.verabackend.domain.port.out.AnalysisRepository;
import com.unlam.verabackend.infrastructure.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.security.access.AccessDeniedException;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Pruebas Unitarias para ManageAnalysisUseCaseImpl")
class ManageAnalysisUseCaseImplTest {

    @Mock
    private AnalysisRepository analysisRepository;

    @InjectMocks
    private ManageAnalysisUseCaseImpl manageAnalysisUseCase;

    private String userEmail;
    private Analysis mockAnalysis;
    private UUID analysisId;

    @BeforeEach
    void setUp() {
        userEmail = "operador@unlam.edu.ar";
        analysisId = UUID.randomUUID();

        User mockUser = new User();
        mockUser.setId(1L);
        mockUser.setEmail(userEmail);

        mockAnalysis = new Analysis();
        mockAnalysis.setId(analysisId);
        mockAnalysis.setUser(mockUser);
    }

    @Test
    @DisplayName("Debería retornar una página con el historial de análisis filtrado bajo los criterios especificados")
    void getAnalysisHistory_ValidScenario_ShouldReturnHistoryPage() {
        // Arrange
        Page<Analysis> expectedPage = new PageImpl<>(List.of(mockAnalysis));
        when(analysisRepository.findByCriteria(userEmail, RiskLevel.HIGH, "patrón", 0))
                .thenReturn(expectedPage);

        // Act
        Page<Analysis> result = manageAnalysisUseCase.getAnalysisHistory(userEmail, RiskLevel.HIGH, "patrón", 0);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(analysisRepository, times(1)).findByCriteria(userEmail, RiskLevel.HIGH, "patrón", 0);
    }

    @Test
    @DisplayName("Debería retornar el detalle de un análisis si el recurso existe y le pertenece al usuario")
    void getAnalysisDetail_ValidScenario_ShouldReturnAnalysis() {
        // Arrange
        when(analysisRepository.findById(analysisId)).thenReturn(Optional.of(mockAnalysis));

        // Act
        Analysis result = manageAnalysisUseCase.getAnalysisDetail(analysisId, userEmail);

        // Assert
        assertNotNull(result);
        assertEquals(analysisId, result.getId());
        assertEquals(userEmail, result.getUser().getEmail());
    }

    @Test
    @DisplayName("Debería remover definitivamente el análisis si el recurso existe y pasa los controles de propiedad")
    void deleteAnalysis_ValidScenario_ShouldDeleteSuccessfully() {
        // Arrange
        when(analysisRepository.findById(analysisId)).thenReturn(Optional.of(mockAnalysis));
        doNothing().when(analysisRepository).deleteById(analysisId);

        // Act
        manageAnalysisUseCase.deleteAnalysis(analysisId, userEmail);

        // Assert
        verify(analysisRepository, times(1)).findById(analysisId);
        verify(analysisRepository, times(1)).deleteById(analysisId);
    }

    @Test
    @DisplayName("Debería lanzar ResourceNotFoundException si el ID del análisis consultado no existe en la persistencia")
    void validateAndGetOwnedAnalysis_NotFound_ShouldThrowResourceNotFoundException() {
        // Arrange
        UUID nonexistentId = UUID.randomUUID();
        when(analysisRepository.findById(nonexistentId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () ->
                manageAnalysisUseCase.getAnalysisDetail(nonexistentId, userEmail)
        );
        verify(analysisRepository, never()).deleteById(any());
    }

    @Test
    @DisplayName("Debería lanzar AccessDeniedException si un usuario malicioso intenta leer o alterar un análisis ajeno")
    void validateAndGetOwnedAnalysis_NotOwner_ShouldThrowAccessDeniedException() {
        // Arrange
        User strangerUser = new User();
        strangerUser.setEmail("stranger@unlam.edu.ar");
        mockAnalysis.setUser(strangerUser);

        when(analysisRepository.findById(analysisId)).thenReturn(Optional.of(mockAnalysis));

        // Act & Assert
        AccessDeniedException exception = assertThrows(AccessDeniedException.class, () ->
                manageAnalysisUseCase.getAnalysisDetail(analysisId, userEmail)
        );

        assertEquals("No tenés permisos para ver este análisis.", exception.getMessage());
        verify(analysisRepository, never()).deleteById(any());
    }
}