package com.unlam.verabackend.presentation.mapper;

import com.unlam.verabackend.domain.model.AlertDetail;
import com.unlam.verabackend.presentation.dto.AlertDetailPresentation;

public class AlertPresentationMapper {

    private AlertPresentationMapper() {}

    public static AlertDetailPresentation toDetailPresentation(AlertDetail detail) {
        return new AlertDetailPresentation(
                detail.getAlertId(),
                detail.getAnalysisId(),
                detail.getMessageContent(),
                detail.getMessageSource().name(),
                detail.getRiskLevel().name(),
                detail.getRiskLevel().getDisplayName(),
                detail.getSuspiciousPatterns(),
                detail.getRecommendation(),
                detail.isSolved(),
                detail.getCreatedAt()
        );
    }
}