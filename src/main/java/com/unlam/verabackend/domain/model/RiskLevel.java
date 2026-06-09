package com.unlam.verabackend.domain.model;

public enum RiskLevel {
    LOW, // Riesgo bajo: No se detectaron patrones sospechosos ni enlaces maliciosos. El contenido parece seguro.
    MEDIUM, // Riesgo medio: Se detectaron algunos patrones sospechosos o el contenido es dudoso, pero no hay evidencia clara de estafa o malware. Se recomienda precaución.
    HIGH // Riesgo alto: Se detectaron patrones claros de estafa, enlaces maliciosos activos o contenido peligroso. Se recomienda no interactuar y eliminar el mensaje.
}