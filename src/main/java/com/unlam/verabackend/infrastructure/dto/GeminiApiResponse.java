package com.unlam.verabackend.infrastructure.dto;

import java.util.List;

public record GeminiApiResponse(List<Candidate> candidates) {

    public record Candidate(Content content) {}
    public record Content(List<Part> parts) {}
    public record Part(String text) {}

    public String getFirstText() {
        if (candidates != null && !candidates.isEmpty() &&
                candidates.getFirst().content() != null &&
                candidates.getFirst().content().parts() != null &&
                !candidates.getFirst().content().parts().isEmpty()) {

            return candidates.getFirst().content().parts().getFirst().text();
        }
        return null;
    }
}
