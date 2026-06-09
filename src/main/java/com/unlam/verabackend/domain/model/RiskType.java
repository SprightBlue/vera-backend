package com.unlam.verabackend.domain.model;

public enum RiskType {
    NONE, // No se detectó ningún riesgo
    PHISHING, // Intento de suplantación de identidad para obtener información sensible
    SMISHING, // Phishing a través de mensajes SMS
    VISHING, // Phishing a través de llamadas telefónicas
    FINANCIAL_FRAUD, // Estafas financieras, como falsas inversiones o préstamos
    IDENTITY_THEFT, // Robo de identidad, donde se intenta obtener datos personales para suplantar a la víctima
    MALWARE_LINK // Enlaces que conducen a la descarga de malware o software malicioso
}