package com.unlam.verabackend.analysis.infrastructure.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class GeminiApiResponse {

    private List<Candidate> candidates;

    @Getter
    @Setter
    @NoArgsConstructor
    public static class Candidate {
        private Content content;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class Content {
        private List<Part> parts;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class Part {
        private String text;
    }
}

