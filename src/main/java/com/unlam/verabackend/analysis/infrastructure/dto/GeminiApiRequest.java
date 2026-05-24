package com.unlam.verabackend.analysis.infrastructure.dto;

import lombok.Getter;

import java.util.List;

@Getter
public class GeminiApiRequest {

    private final List<Content> contents;

    public GeminiApiRequest(String prompt) {
        this.contents = List.of(new Content(List.of(new Part(prompt))));
    }

    @Getter
    public static class Content {
        private final List<Part> parts;

        public Content(List<Part> parts) {
            this.parts = parts;
        }
    }

    @Getter
    public static class Part {
        private final String text;

        public Part(String text) {
            this.text = text;
        }
    }
}

