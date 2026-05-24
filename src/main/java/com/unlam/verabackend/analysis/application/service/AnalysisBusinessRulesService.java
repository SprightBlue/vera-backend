package com.unlam.verabackend.analysis.application.service;

import com.unlam.verabackend.analysis.domain.model.RiskLevel;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class AnalysisBusinessRulesService {

    private static final Pattern URL_PATTERN = Pattern.compile("https?://\\S+", Pattern.CASE_INSENSITIVE);

    public void validateMessageContent(String content) {
        if (content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException("El mensaje no puede estar vacio");
        }
    }

    public Optional<String> extractFirstUrl(String content) {
        Matcher matcher = URL_PATTERN.matcher(content);
        if (matcher.find()) {
            return Optional.of(matcher.group());
        }
        return Optional.empty();
    }

    public RiskLevel resolveRiskLevel(RiskLevel geminiRiskLevel, boolean maliciousUrlDetected) {
        if (maliciousUrlDetected) {
            return RiskLevel.HIGH;
        }

        return geminiRiskLevel;
    }

    public boolean resolveThreat(boolean geminiThreat, boolean maliciousUrlDetected) {
        if (maliciousUrlDetected) {
            return true;
        }

        return geminiThreat;
    }
}
