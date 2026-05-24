package com.unlam.verabackend.analysis.infrastructure.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class SafeBrowsingResponse {

    private List<ThreatMatch> matches;

    @Getter
    @Setter
    @NoArgsConstructor
    public static class ThreatMatch {
        private String threatType;
        private String platformType;
        private String threatEntryType;
        private ThreatEntry threat;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class ThreatEntry {
        private String url;
    }
}

