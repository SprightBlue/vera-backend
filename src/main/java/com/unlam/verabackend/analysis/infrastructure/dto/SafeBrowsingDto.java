package com.unlam.verabackend.analysis.infrastructure.dto;

import java.util.List;


public record SafeBrowsingDto(
        boolean malicious,
        int matchCount,
        List<String> threatTypes,
        List<String> matchedUrls
) {

    public static SafeBrowsingDto empty() {
        return new SafeBrowsingDto(false, 0, List.of(), List.of());
    }
}
