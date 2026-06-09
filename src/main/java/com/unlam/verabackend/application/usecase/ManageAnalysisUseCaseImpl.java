package com.unlam.verabackend.application.usecase;

import com.unlam.verabackend.domain.exception.ResourceNotFoundException;
import com.unlam.verabackend.domain.model.Analysis;
import com.unlam.verabackend.domain.port.in.ManageAnalysisUseCase;
import com.unlam.verabackend.domain.port.out.AnalysisRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class ManageAnalysisUseCaseImpl implements ManageAnalysisUseCase {

    private final AnalysisRepository analysisRepository;

    public ManageAnalysisUseCaseImpl(AnalysisRepository analysisRepository) {
        this.analysisRepository = analysisRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Analysis> getHistoryByUserEmail(String email, Pageable pageable) {
        return analysisRepository.findByUserEmailOrderByCreatedAtDesc(email, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Analysis> getHistoryByUserEmailAndRiskLevel(String email, String riskLevel, Pageable pageable) {
        return analysisRepository.findByUserEmailAndRiskLevelOrderByCreatedAtDesc(email, riskLevel.toUpperCase().strip(), pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Analysis getAnalysisDetail(UUID id, String userEmail) {
        Analysis analysis = analysisRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("El análisis solicitado no existe."));

        if (!analysis.getUser().getEmail().equals(userEmail)) {
            throw new AccessDeniedException("No tenés permisos para ver este análisis.");
        }
        return analysis;
    }

    @Override
    @Transactional
    public void deleteAnalysis(UUID id, String userEmail) {
        Analysis analysis = getAnalysisDetail(id, userEmail);
        analysisRepository.deleteById(analysis.getId());
    }
}