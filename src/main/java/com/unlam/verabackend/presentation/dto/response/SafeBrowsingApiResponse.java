package com.unlam.verabackend.presentation.dto.response;

import java.util.List;

public record SafeBrowsingApiResponse(List<ThreatMatch> matches) {

    public record ThreatMatch(
            String threatType,
            String platformType,
            String threatEntryType,
            ThreatEntry threat
    ) {}

    public record ThreatEntry(String url) {}
}
