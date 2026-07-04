package com.unlam.verabackend.infrastructure.mapper;

import com.unlam.verabackend.domain.model.Analysis;
import com.unlam.verabackend.infrastructure.entity.AnalysisEntity;
import com.unlam.verabackend.infrastructure.entity.User;
import org.springframework.stereotype.Component;

@Component
public class AnalysisMapper {

    public AnalysisEntity toEntity(Analysis domain, User userEntity) {
        if (domain == null) return null;

        return AnalysisEntity.builder()
                .id(domain.getId())
                .user(userEntity)
                .title(domain.getTitle())
                .source(domain.getSource())
                .contentSummary(domain.getContentSummary())
                .riskType(domain.getRiskType() != null ? domain.getRiskType() : null)
                .riskLevel(domain.getRiskLevel())
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
                .source(entity.getSource())
                .contentSummary(entity.getContentSummary())
                .riskType(entity.getRiskType() != null ? entity.getRiskType() : null)
                .riskLevel(entity.getRiskLevel())
                .riskPercentage(entity.getRiskPercentage())
                .suspiciousPatterns(entity.getSuspiciousPatterns())
                .recommendation(entity.getRecommendation())
                .build();
    }
}