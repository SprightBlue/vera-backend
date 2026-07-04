package com.unlam.verabackend.application.usecase;

import com.unlam.verabackend.domain.exception.ResourceNotFoundException;
import com.unlam.verabackend.domain.model.Analysis;
import com.unlam.verabackend.domain.model.RiskLevel;
import com.unlam.verabackend.domain.port.out.AnalysisRepository;
import com.unlam.verabackend.infrastructure.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.security.access.AccessDeniedException;

import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ManageAnalysisUseCaseImplTest {

    @Mock
    private AnalysisRepository analysisRepository;

    @InjectMocks
    private ManageAnalysisUseCaseImpl manageAnalysisUseCase;

    private final String userEmail = "test@unlam.edu.ar";

    @Test
    @DisplayName("Debe retornar la página de análisis invocando al repositorio con los filtros dinámicos")
    void getAnalysisHistory_WhenCalled_ShouldReturnPagedAnalysis() {
        // Arrange
        RiskLevel riskLevel = RiskLevel.HIGH;
        String search = "alerta";
        int page = 0;
        Page<Analysis> expectedPage = new PageImpl<>(Collections.emptyList());

        when(analysisRepository.findByCriteria(userEmail, riskLevel, search, page))
                .thenReturn(expectedPage);

        // Act
        Page<Analysis> result = manageAnalysisUseCase.getAnalysisHistory(userEmail, riskLevel, search, page);

        // Assert
        assertNotNull(result);
        assertEquals(expectedPage, result);
        verify(analysisRepository, times(1))
                .findByCriteria(userEmail, riskLevel, search, page);
    }

    @Test
    @DisplayName("Debe lanzar ResourceNotFoundException cuando el análisis no existe en la base de datos")
    void getAnalysisDetail_WhenAnalysisDoesNotExist_ShouldThrowResourceNotFoundException() {
        // Arrange
        UUID id = UUID.randomUUID();
        when(analysisRepository.findById(id)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () ->
                manageAnalysisUseCase.getAnalysisDetail(id, userEmail)
        );
        assertEquals("El análisis solicitado no existe.", exception.getMessage());
        verify(analysisRepository, times(1)).findById(id);
    }

    @Test
    @DisplayName("Debe lanzar AccessDeniedException (403) cuando un usuario intenta ver un análisis de otra cuenta")
    void getAnalysisDetail_WhenUserDoesNotHavePermissions_ShouldThrowAccessDeniedException() {
        // Arrange
        UUID id = UUID.randomUUID();

        User ownerUser = new User();
        ownerUser.setEmail("otro-usuario@unlam.edu.ar");

        Analysis analysis = new Analysis();
        analysis.setUser(ownerUser);

        when(analysisRepository.findById(id)).thenReturn(Optional.of(analysis));

        // Act & Assert
        AccessDeniedException exception = assertThrows(AccessDeniedException.class, () ->
                manageAnalysisUseCase.getAnalysisDetail(id, userEmail)
        );
        assertEquals("No tenés permisos para ver este análisis.", exception.getMessage());
    }

    @Test
    @DisplayName("Debe retornar el análisis exitosamente si el usuario es el dueño legítimo")
    void getAnalysisDetail_WhenAnalysisExistsAndUserIsOwner_ShouldReturnAnalysis() {
        // Arrange
        UUID id = UUID.randomUUID();

        User ownerUser = new User();
        ownerUser.setEmail(userEmail);

        Analysis expectedAnalysis = new Analysis();
        expectedAnalysis.setUser(ownerUser);

        when(analysisRepository.findById(id)).thenReturn(Optional.of(expectedAnalysis));

        // Act
        Analysis result = manageAnalysisUseCase.getAnalysisDetail(id, userEmail);

        // Assert
        assertNotNull(result);
        assertEquals(expectedAnalysis, result);
    }

    @Test
    @DisplayName("Debe eliminar el análisis físicamente si el solicitante es el dueño")
    void deleteAnalysis_WhenUserIsOwner_ShouldDeleteSuccessfully() {
        // Arrange
        UUID id = UUID.randomUUID();

        User ownerUser = new User();
        ownerUser.setEmail(userEmail);

        Analysis analysis = new Analysis();
        analysis.setId(id);
        analysis.setUser(ownerUser);

        when(analysisRepository.findById(id)).thenReturn(Optional.of(analysis));
        doNothing().when(analysisRepository).deleteById(id);

        // Act & Assert
        assertDoesNotThrow(() -> manageAnalysisUseCase.deleteAnalysis(id, userEmail));

        verify(analysisRepository, times(1)).deleteById(id);
    }
}