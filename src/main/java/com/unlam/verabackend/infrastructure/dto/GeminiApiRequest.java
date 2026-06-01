package com.unlam.verabackend.infrastructure.dto;

import java.util.List;

public record GeminiApiRequest(
        List<Content> contents,
        GenerationConfig generationConfig
) {
    public record Content(List<Part> parts) {}
    public record Part(String text) {}

    public record GenerationConfig(
            String responseMimeType,
            Double temperature
    ) {}

    public static GeminiApiRequest forPrompt(String prompt) {
        Part part = new Part(prompt);
        Content content = new Content(List.of(part));
        GenerationConfig config = new GenerationConfig("application/json", 0.1);

        return new GeminiApiRequest(List.of(content), config);
    }
}
