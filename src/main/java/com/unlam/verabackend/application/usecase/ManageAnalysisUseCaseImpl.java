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
        log.info("UseCase: Extrayendo historial de análisis por criterios para el usuario [{}]", email);
        return analysisRepository.findByCriteria(email, riskLevel, search, page);
    }

    @Override
    @Transactional(readOnly = true)
    public Analysis getAnalysisDetail(UUID id, String userEmail) {
        log.info("UseCase: Solicitando acceso al detalle del análisis heurístico ID [{}]", id);
        return validateAndGetOwnedAnalysis(id, userEmail);
    }

    @Override
    @Transactional
    public void deleteAnalysis(UUID id, String userEmail) {
        log.info("UseCase: Iniciando remoción definitiva del análisis ID [{}] solicitado por [{}]", id, userEmail);

        Analysis analysis = validateAndGetOwnedAnalysis(id, userEmail);
        analysisRepository.deleteById(analysis.getId());

        log.info("UseCase: Análisis ID [{}] eliminado exitosamente de la persistencia.", id);
    }

    private Analysis validateAndGetOwnedAnalysis(UUID id, String userEmail) {
        Analysis analysis = analysisRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("UseCase Error: El análisis solicitado con ID [{}] no fue localizado.", id);
                    return new ResourceNotFoundException("El análisis solicitado no existe.");
                });

        if (!analysis.getUser().getEmail().equalsIgnoreCase(userEmail)) {
            log.warn("ALERTA DE SEGURIDAD: El operador [{}] intentó inspeccionar el análisis heurístico ajeno ID [{}]",
                    userEmail, id);
            throw new AccessDeniedException("No tenés permisos para ver este análisis.");
        }
        return analysis;
    }
}