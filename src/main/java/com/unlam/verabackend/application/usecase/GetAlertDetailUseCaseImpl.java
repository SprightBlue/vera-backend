package com.unlam.verabackend.application.usecase;

import com.unlam.verabackend.domain.model.AlertDetail;
import com.unlam.verabackend.domain.model.RiskAlert;
import com.unlam.verabackend.domain.ports.in.GetAlertDetailUseCase;
import com.unlam.verabackend.domain.ports.out.RiskAlertRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.AccessDeniedException;
import java.util.UUID;

@Service
public class GetAlertDetailUseCaseImpl implements GetAlertDetailUseCase {

    private final RiskAlertRepository riskAlertRepository;

    public GetAlertDetailUseCaseImpl(RiskAlertRepository riskAlertRepository) {
        this.riskAlertRepository = riskAlertRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public AlertDetail getDetail(UUID alertId, Long requestingUserId) throws AccessDeniedException {
        RiskAlert alert = riskAlertRepository.findById(alertId.toString())
                .orElseThrow(() -> new IllegalArgumentException("Alerta no encontrada: " + alertId));

        if (!alert.getCaregiver().getId().equals(requestingUserId)) {
            throw new AccessDeniedException("No tenés permiso para ver esta alerta");
        }

        return new AlertDetail(
                alert.getId(),
                alert.getAnalysis().getId(),
                alert.getAnalysis().getContent(),
                alert.getAnalysis().getMessageSource(),
                alert.getAnalysis().getRiskLevel(),
                alert.getAnalysis().getSuspiciousPatterns(),
                alert.getAnalysis().getRecommendation(),
                alert.isSolved(),
                alert.getCreatedAt()
        );
    }
}