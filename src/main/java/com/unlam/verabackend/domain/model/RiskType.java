package com.unlam.verabackend.domain.model;

import lombok.Getter;

@Getter
public enum RiskType {
    // Interacciones Técnicas
    CLICKED_SUSPICIOUS_LINK("Enlace sospechoso"),
    DOWNLOADED_FILE_OR_APP("Archivo o aplicación sospechosa"),

    // Compromiso de Activos
    SHARED_PERSONAL_OR_BANKING_DATA("Datos personales o bancarios expuestos"),
    TRANSFERRED_MONEY("Dinero transferido (Fraude financiero)"),

    // Vectores de Ingeniería Social
    IDENTITY_THEFT_OR_IMPERSONATION("Suplantación de identidad de un tercero"),
    SPOOFED_OFFICIAL_ENTITY("Simulación de entidad u organismo oficial"),
    EXTORTION_OR_COERCION("Extorsión, intimidación o amenaza"),

    // Estados Informativos / Preventivos
    SUSPICIOUS_COMMUNICATION("Patrones de comunicación inusuales"),
    NONE("Ninguno");

    private final String displayName;

    RiskType(String displayName) {
        this.displayName = displayName;
    }

}