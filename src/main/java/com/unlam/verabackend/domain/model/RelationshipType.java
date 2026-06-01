package com.unlam.verabackend.domain.model;

import lombok.Getter;

@Getter
public enum RelationshipType {
    UNDEFINED("Sin Definir"),
    FAMILY_MEMBER("Familiar"),
    TRUSTED_CONTACT("Contacto de Confianza"),
    PROFESSIONAL("Soporte Profesional");

    private final String displayName;

    RelationshipType(String displayName) { this.displayName = displayName; }

    public static RelationshipType fromString(String value) {
        if (value == null) return UNDEFINED;
        for (RelationshipType type : RelationshipType.values()) {
            if (type.name().equalsIgnoreCase(value.trim())) return type;
        }
        return UNDEFINED;
    }
}
