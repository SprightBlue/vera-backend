package com.unlam.verabackend.application.usecase;

import com.unlam.verabackend.domain.exception.ResourceNotFoundException;
import com.unlam.verabackend.domain.model.Analysis;
import com.unlam.verabackend.domain.model.RiskLevel;
import com.unlam.verabackend.domain.port.in.ManageAnalysisUseCase;
import com.unlam.verabackend.domain.port.out.AnalysisRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ManageAnalysisUseCaseImpl implements ManageAnalysisUseCase {

    private final AnalysisRepository analysisRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<Analysis> getAnalysisHistory(String email, RiskLevel riskLevel, String search, int page) {
        log.info("Cargando historial de análisis del usuario: {}. Filtros -> RiskLevel: {}, Search: {}, Page: {}", email, riskLevel, search, page);
        return analysisRepository.findByCriteria(email, riskLevel, search, page);
    }

    @Override
    @Transactional(readOnly = true)
    public Analysis getAnalysisDetail(UUID id, String userEmail) {
        Analysis analysis = analysisRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Análisis no encontrado con ID: {}", id);
                    return new ResourceNotFoundException("El análisis solicitado no existe.");
                });

        if (!analysis.getUser().getEmail().equals(userEmail)) {
            log.error("VIOLACIÓN DE SEGURIDAD: Usuario {} intentó leer el análisis ajeno ID: {}", userEmail, id);
            throw new AccessDeniedException("No tenés permisos para ver este análisis.");
        }
        return analysis;
    }

    @Override
    @Transactional
    public void deleteAnalysis(UUID id, String userEmail) {
        Analysis analysis = getAnalysisDetail(id, userEmail);
        analysisRepository.deleteById(analysis.getId());
        log.info("Análisis ID: {} eliminado con éxito por el usuario: {}", id, userEmail);
    }
}