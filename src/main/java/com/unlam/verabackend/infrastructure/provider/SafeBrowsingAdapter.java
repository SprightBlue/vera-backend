package com.unlam.verabackend.infrastructure.provider;

import com.unlam.verabackend.domain.port.out.SafeBrowsingProvider;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Component
public class SafeBrowsingAdapter implements SafeBrowsingProvider {

    private final RestTemplate restTemplate;

    @Value("${google.safe-browsing.api-key}")
    private String apiKey;

    private static final String GOOGLE_API_URL = "https://safebrowsing.googleapis.com/v4/threatMatches:find?key=";

    public SafeBrowsingAdapter() {
        this.restTemplate = new RestTemplate();
    }

    @Override
    public List<String> checkUrls(List<String> urls) {
        List<String> threatsFound = new ArrayList<>();

        if (urls == null || urls.isEmpty()) {
            return threatsFound;
        }

        String finalUrl = GOOGLE_API_URL + apiKey;

        Map<String, Object> requestBody = new HashMap<>();

        Map<String, Object> clientInfo = new HashMap<>();
        clientInfo.put("clientId", "vera-backend-unlam");
        clientInfo.put("clientVersion", "1.0.0");
        requestBody.put("client", clientInfo);

        Map<String, Object> threatInfo = new HashMap<>();
        threatInfo.put("threatTypes", Arrays.asList(
                "MALWARE",
                "SOCIAL_ENGINEERING",
                "UNWANTED_SOFTWARE",
                "POTENTIALLY_HARMFUL_APPLICATION"
        ));
        threatInfo.put("platformTypes", List.of("ANY_PLATFORM"));
        threatInfo.put("threatEntryTypes", List.of("URL"));

        List<Map<String, String>> threatEntries = new ArrayList<>();
        for (String urlStr : urls) {
            Map<String, String> entry = new HashMap<>();
            entry.put("url", urlStr);
            threatEntries.add(entry);
        }
        threatInfo.put("threatEntries", threatEntries);
        requestBody.put("threatInfo", threatInfo);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        try {
            SafeBrowsingResponse response = restTemplate.postForObject(finalUrl, entity, SafeBrowsingResponse.class);

            if (response != null && response.getMatches() != null) {
                for (Match match : response.getMatches()) {
                    String threatUrl = null;
                    if (match.getThreat() != null) {
                        threatUrl = match.getThreat().getUrl();
                    }
                    String threatType = match.getThreatType();

                    threatsFound.add("- La URL [" + threatUrl + "] fue catalogada por Google como una amenaza de tipo: " + threatType);
                }
            }
        } catch (Exception e) {
            threatsFound.add("AVISO: No se pudo verificar la reputación online de los enlaces por un problema de comunicación con Google Safe Browsing.");
        }

        return threatsFound;
    }

    @Setter
    @Getter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class SafeBrowsingResponse {
        private List<Match> matches;

    }

    @Setter
    @Getter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Match {
        private Threat threat;
        private String threatType;

    }

    @Setter
    @Getter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Threat {
        private String url;

    }
}