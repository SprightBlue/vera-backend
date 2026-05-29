package com.unlam.verabackend.domain.model;

import lombok.Getter;

@Getter
public enum RiskLevel {
    UNDEFINED("Sin Definir"),
    LOW("Bajo"),
    MEDIUM("Medio"),
    HIGH("Alto");

    private final String displayName;

    RiskLevel(String displayName) { this.displayName = displayName; }

    public static RiskLevel fromString(String value) {
        if (value == null) return UNDEFINED;
        for (RiskLevel level : RiskLevel.values()) {
            if (level.name().equalsIgnoreCase(value.trim())) return level;
        }
        return UNDEFINED;
    }
}
