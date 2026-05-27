package com.unlam.verabackend.analysis.infrastructure.dto;

import java.util.List;

public record SafeBrowsingApiRequest(
        ClientInfo client,
        ThreatInfo threatInfo
) {
    public record ClientInfo(String clientId, String clientVersion) {}

    public record ThreatInfo(
            List<String> threatTypes,
            List<String> platformTypes,
            List<String> threatEntryTypes,
            List<ThreatEntry> threatEntries
    ) {}

    public record ThreatEntry(String url) {}

    public static SafeBrowsingApiRequest forUrls(List<String> urls) {
        ClientInfo client = new ClientInfo("verabackend", "1.0.0");

        List<ThreatEntry> entries = urls.stream()
                .map(ThreatEntry::new)
                .toList();

        ThreatInfo threatInfo = new ThreatInfo(
                List.of("MALWARE", "SOCIAL_ENGINEERING", "UNWANTED_SOFTWARE", "POTENTIALLY_HARMFUL_APPLICATION"),
                List.of("ANY_PLATFORM"),
                List.of("URL"),
                entries
        );

        return new SafeBrowsingApiRequest(client, threatInfo);
    }
}
