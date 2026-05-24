package com.unlam.verabackend.analysis.infrastructure.dto;

import lombok.Getter;

import java.util.List;

@Getter
public class SafeBrowsingRequest {

    private final Client client;
    private final ThreatInfo threatInfo;

    private SafeBrowsingRequest(Client client, ThreatInfo threatInfo) {
        this.client = client;
        this.threatInfo = threatInfo;
    }

    public static SafeBrowsingRequest forUrl(String url) {
        return new SafeBrowsingRequest(
                new Client("vera-backend", "1.0.0"),
                new ThreatInfo(
                        List.of("MALWARE", "SOCIAL_ENGINEERING", "UNWANTED_SOFTWARE", "POTENTIALLY_HARMFUL_APPLICATION"),
                        List.of("ANY_PLATFORM"),
                        List.of("URL"),
                        List.of(new ThreatEntry(url))
                )
        );
    }

    @Getter
    public static class Client {
        private final String clientId;
        private final String clientVersion;

        public Client(String clientId, String clientVersion) {
            this.clientId = clientId;
            this.clientVersion = clientVersion;
        }
    }

    @Getter
    public static class ThreatInfo {
        private final List<String> threatTypes;
        private final List<String> platformTypes;
        private final List<String> threatEntryTypes;
        private final List<ThreatEntry> threatEntries;

        public ThreatInfo(List<String> threatTypes,
                             List<String> platformTypes,
                             List<String> threatEntryTypes,
                             List<ThreatEntry> threatEntries) {
            this.threatTypes = threatTypes;
            this.platformTypes = platformTypes;
            this.threatEntryTypes = threatEntryTypes;
            this.threatEntries = threatEntries;
        }
    }

    @Getter
    public static class ThreatEntry {
        private final String url;

        public ThreatEntry(String url) {
            this.url = url;
        }
    }
}

