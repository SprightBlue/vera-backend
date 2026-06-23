package com.unlam.verabackend.application.usecase;

import com.unlam.verabackend.domain.exception.ResourceNotFoundException;
import com.unlam.verabackend.domain.model.Analysis;
import com.unlam.verabackend.domain.model.RiskLevel;
import com.unlam.verabackend.domain.port.out.AnalysisRepository;
import com.unlam.verabackend.infrastructure.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
    private Pageable pageable;

    @BeforeEach
    void setUp() {
        pageable = PageRequest.of(0, 10);
    }

    // ==========================================
    // Tests para getHistoryByUserEmail()
    // ==========================================

    @Test
    void getHistoryByUserEmail_WhenCalled_ShouldReturnPagedAnalysis() {
        // Arrange
        Page<Analysis> expectedPage = new PageImpl<>(Collections.emptyList());
        when(analysisRepository.findByUserEmailOrderByCreatedAtDesc(userEmail, pageable))
                .thenReturn(expectedPage);

        // Act
        Page<Analysis> result = manageAnalysisUseCase.getHistoryByUserEmail(userEmail, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(expectedPage, result);
        verify(analysisRepository, times(1))
                .findByUserEmailOrderByCreatedAtDesc(userEmail, pageable);
    }

    // ==========================================
    // Tests para getHistoryByUserEmailAndRiskLevel()
    // ==========================================

    @Test
    void getHistoryByUserEmailAndRiskLevel_WhenCalled_ShouldReturnPagedAnalysisFiltered() {
        // Arrange
        RiskLevel riskLevel = RiskLevel.HIGH;
        Page<Analysis> expectedPage = new PageImpl<>(Collections.emptyList());
        when(analysisRepository.findByUserEmailAndRiskLevelOrderByCreatedAtDesc(userEmail, riskLevel, pageable))
                .thenReturn(expectedPage);

        // Act
        Page<Analysis> result = manageAnalysisUseCase.getHistoryByUserEmailAndRiskLevel(userEmail, riskLevel, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(expectedPage, result);
        verify(analysisRepository, times(1))
                .findByUserEmailAndRiskLevelOrderByCreatedAtDesc(userEmail, riskLevel, pageable);
    }

    // ==========================================
    // Tests para getAnalysisDetail()
    // ==========================================

    @Test
    void getAnalysisDetail_WhenAnalysisDoesNotExist_ShouldThrowResourceNotFoundException() {
        // Arrange
        UUID id = UUID.randomUUID();
        when(analysisRepository.findById(id)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> manageAnalysisUseCase.getAnalysisDetail(id, userEmail));
        assertEquals("El análisis solicitado no existe.", exception.getMessage());
        verify(analysisRepository, times(1)).findById(id);
    }

    @Test
    void getAnalysisDetail_WhenUserDoesNotHavePermissions_ShouldThrowAccessDeniedException() {
        // Arrange
        UUID id = UUID.randomUUID();

        User ownerUser = new User();
        ownerUser.setEmail("otro-usuario@unlam.edu.ar");

        Analysis analysis = new Analysis();
        analysis.setUser(ownerUser);

        when(analysisRepository.findById(id)).thenReturn(Optional.of(analysis));

        // Act & Assert
        AccessDeniedException exception = assertThrows(AccessDeniedException.class, () -> manageAnalysisUseCase.getAnalysisDetail(id, userEmail));
        assertEquals("No tenés permisos para ver este análisis.", exception.getMessage());
    }

    @Test
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

    // ==========================================
    // Tests para deleteAnalysis()
    // ==========================================

    @Test
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