package com.unlam.verabackend.domain.model;

import java.util.List;

public record UrlValidation(
        boolean malicious,
        List<String> threatTypes
) {
    public static UrlValidation empty() {
        return new UrlValidation(false, List.of());
    }
}
