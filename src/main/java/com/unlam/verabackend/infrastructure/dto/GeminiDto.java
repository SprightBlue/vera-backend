package com.unlam.verabackend.infrastructure.dto;

import com.fasterxml.jackson.annotation.JsonAlias;

public record GeminiDto(
		@JsonAlias({"riskLevel", "risk_level"})
		String riskLevel,

		@JsonAlias({"suspiciousPatterns", "suspicious_patterns"})
		String suspiciousPatterns,

		@JsonAlias({"recommendation", "recommendation_details"})
		String recommendation
) {
	public static GeminiDto fallback() {
		return new GeminiDto(
				"UNDEFINED",
				"Hay detalles del mensaje que conviene revisar con calma.",
				"Si te genera dudas, no respondas enseguida y consultalo con alguien de confianza."
		);
	}
}
