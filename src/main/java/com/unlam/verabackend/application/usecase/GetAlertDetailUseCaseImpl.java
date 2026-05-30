package com.unlam.verabackend.application.usecase;

import com.unlam.verabackend.domain.exception.ResourceNotFoundException;
import com.unlam.verabackend.domain.model.AlertDetail;
import com.unlam.verabackend.domain.model.RiskAlert;
import com.unlam.verabackend.domain.ports.in.GetAlertDetailUseCase;
import com.unlam.verabackend.domain.ports.out.RiskAlertRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class GetAlertDetailUseCaseImpl implements GetAlertDetailUseCase {

    private final RiskAlertRepository riskAlertRepository;

    public GetAlertDetailUseCaseImpl(RiskAlertRepository riskAlertRepository) {
        this.riskAlertRepository = riskAlertRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public AlertDetail getDetail(UUID alertId, Long requestingUserId) {
        RiskAlert alert = riskAlertRepository.findById(alertId.toString())
                .orElseThrow(() -> new ResourceNotFoundException("Alerta no encontrada: " + alertId));

        if (!alert.getCaregiver().getId().equals(requestingUserId)) {
            throw new ResourceNotFoundException("Alerta no encontrada");
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