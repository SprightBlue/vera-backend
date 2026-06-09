package com.unlam.verabackend.infrastructure.mapper;

import com.unlam.verabackend.domain.model.Analysis;
import com.unlam.verabackend.domain.model.RiskLevel;
import com.unlam.verabackend.domain.model.RiskType;
import com.unlam.verabackend.domain.model.Source;
import com.unlam.verabackend.infrastructure.entity.AnalysisEntity;
import com.unlam.verabackend.infrastructure.entity.User;
import org.springframework.stereotype.Component;

@Component
public class AnalysisMapper {

    public AnalysisEntity toEntity(Analysis domain, User realUser) {
        if (domain == null) return null;

        return AnalysisEntity.builder()
                .id(domain.getId())
                .user(realUser)
                .createdAt(domain.getCreatedAt())
                .title(domain.getTitle())
                .source(domain.getSource() != null ? domain.getSource().name() : null)
                .contentSummary(domain.getContentSummary())
                .riskType(domain.getRiskType() != null ? domain.getRiskType().name() : RiskType.NONE.name())
                .riskLevel(domain.getRiskLevel() != null ? domain.getRiskLevel().name() : null)
                .riskPercentage(domain.getRiskPercentage())
                .suspiciousPatterns(domain.getSuspiciousPatterns())
                .recommendation(domain.getRecommendation())
                .build();
    }

    public Analysis toDomain(AnalysisEntity entity) {
        if (entity == null) return null;

        return Analysis.builder()
                .id(entity.getId())
                .user(entity.getUser())
                .createdAt(entity.getCreatedAt())
                .title(entity.getTitle())
                .source(entity.getSource() != null ? Source.valueOf(entity.getSource()) : null)
                .contentSummary(entity.getContentSummary())
                .riskType(entity.getRiskType() != null ? RiskType.valueOf(entity.getRiskType()) : RiskType.NONE)
                .riskLevel(entity.getRiskLevel() != null ? RiskLevel.valueOf(entity.getRiskLevel()) : null)
                .riskPercentage(entity.getRiskPercentage())
                .suspiciousPatterns(entity.getSuspiciousPatterns())
                .recommendation(entity.getRecommendation())
                .build();
    }
}