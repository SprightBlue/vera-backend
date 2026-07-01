package com.unlam.verabackend.infrastructure.provider;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.unlam.verabackend.domain.port.out.CheckUrlProvider;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import java.util.*;

@Slf4j
@Component
public class SbCheckUrlProvider implements CheckUrlProvider {

    private final RestTemplate restTemplate = new RestTemplate();
    @Value("${google.safe-browsing.api-key}")
    private String apiKey;

    @Override
    public List<String> checkUrls(List<String> urls) {
        if (urls == null || urls.isEmpty()) return Collections.emptyList();

        try {
            SafeBrowsingResponse response = restTemplate.postForObject(
                    "https://safebrowsing.googleapis.com/v4/threatMatches:find?key=" + apiKey,
                    buildRequest(urls),
                    SafeBrowsingResponse.class
            );
            return parseMatches(response);
        } catch (Exception e) {
            log.error("Error de comunicación con Google Safe Browsing: {}", e.getMessage());
            return List.of("AVISO: No se pudo verificar la seguridad de los enlaces.");
        }
    }

    private HttpEntity<Map<String, Object>> buildRequest(List<String> urls) {
        Map<String, Object> body = new HashMap<>();
        body.put("client", Map.of("clientId", "vera-backend", "clientVersion", "1.0.0"));
        body.put("threatInfo", Map.of(
                "threatTypes", List.of("MALWARE", "SOCIAL_ENGINEERING", "UNWANTED_SOFTWARE", "POTENTIALLY_HARMFUL_APPLICATION"),
                "platformTypes", List.of("ANY_PLATFORM"),
                "threatEntryTypes", List.of("URL"),
                "threatEntries", urls.stream().map(url -> Map.of("url", url)).toList()
        ));
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new HttpEntity<>(body, headers);
    }

    private List<String> parseMatches(SafeBrowsingResponse res) {
        if (res == null || res.getMatches() == null) return Collections.emptyList();
        return res.getMatches().stream()
                .map(m -> String.format("- URL [%s] es amenaza: %s", m.getThreat().getUrl(), m.getThreatType()))
                .toList();
    }

    @Getter @Setter @JsonIgnoreProperties(ignoreUnknown = true)
    public static class SafeBrowsingResponse { private List<Match> matches; }
    @Getter @Setter @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Match { private Threat threat; private String threatType; }
    @Getter @Setter @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Threat { private String url; }
}