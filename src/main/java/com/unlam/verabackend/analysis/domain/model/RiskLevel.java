package com.unlam.verabackend.analysis.domain.model;

import lombok.Getter;

@Getter
public enum RiskLevel {
    LOW("Bajo", "No encontramos nada fuera de lo común en este mensaje. Podés interactuar con tranquilidad."),
    MEDIUM("Medio", "Este mensaje tiene algunos detalles confusos o solicita cosas con mucha prisa. Te sugerimos mirarlo con atención y, ante la duda, charlarlo con alguien de confianza antes de responder."),
    HIGH("Alto", "Detectamos que este mensaje contiene enlaces o pedidos falsos que podrían no ser seguros. Lo ideal es no hacer clic en los enlaces, evitar compartir datos y borrar el mensaje para estar más tranquilos."),
    UNDEFINED("Indefinido", "No pudimos determinar con claridad el nivel de riesgo de este mensaje. Conviene revisarlo con calma.");

    private final String name;
    private final String description;

    RiskLevel(String name, String description) {
        this.name = name;
        this.description = description;
    }

}
