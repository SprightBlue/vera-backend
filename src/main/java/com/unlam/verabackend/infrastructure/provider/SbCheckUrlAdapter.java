package com.unlam.verabackend.infrastructure.provider;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.unlam.verabackend.domain.port.out.CheckUrlProvider;
import lombok.Getter;
import lombok.Setter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import java.util.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class SbCheckUrlAdapter implements CheckUrlProvider {

    private final RestTemplate restTemplate;

    @Value("${google.safe-browsing.api-key}")
    private String apiKey;

    private static final String SAFE_BROWSING_URL = "https://safebrowsing.googleapis.com/v4/threatMatches:find?key={apiKey}";
    private static final String CLIENT_ID = "vera-backend";
    private static final String CLIENT_VERSION = "1.0.0";
    private static final String NETWORK_ERROR_FALLBACK = "AVISO: No se pudo verificar la seguridad de los enlaces.";

    @Override
    public List<String> checkUrls(List<String> urls) {
        if (urls == null || urls.isEmpty()) {
            return Collections.emptyList();
        }

        log.info("Infrastructure Adapter: Iniciando auditoría de seguridad para [{}] URLs candidatas mediante Google Safe Browsing.", urls.size());

        try {
            HttpEntity<Map<String, Object>> requestEntity = buildSafeBrowsingRequest(urls);
            Map<String, String> uriVariables = Map.of("apiKey", apiKey);

            log.debug("Infrastructure Adapter: Despachando payload POST hacia la API corporativa de Google.");
            SafeBrowsingResponse response = restTemplate.postForObject(
                    SAFE_BROWSING_URL,
                    requestEntity,
                    SafeBrowsingResponse.class,
                    uriVariables
            );

            return processSafeBrowsingMatches(response);

        } catch (Exception e) {
            log.error("Infrastructure Exception: Falló el handshake o la transmisión con Google Safe Browsing. Detalles: {}", e.getMessage(), e);
            return List.of(NETWORK_ERROR_FALLBACK);
        }
    }

    private HttpEntity<Map<String, Object>> buildSafeBrowsingRequest(List<String> urls) {
        List<Map<String, String>> threatEntries = urls.stream()
                .map(url -> Map.of("url", url))
                .toList();

        Map<String, Object> threatInfo = Map.of(
                "threatTypes", List.of("MALWARE", "SOCIAL_ENGINEERING", "UNWANTED_SOFTWARE", "POTENTIALLY_HARMFUL_APPLICATION"),
                "platformTypes", List.of("ANY_PLATFORM"),
                "threatEntryTypes", List.of("URL"),
                "threatEntries", threatEntries
        );

        Map<String, Object> clientInfo = Map.of(
                "clientId", CLIENT_ID,
                "clientVersion", CLIENT_VERSION
        );

        Map<String, Object> requestBody = Map.of(
                "client", clientInfo,
                "threatInfo", threatInfo
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        return new HttpEntity<>(requestBody, headers);
    }

    private List<String> processSafeBrowsingMatches(SafeBrowsingResponse response) {
        if (response == null || response.getMatches() == null) {
            log.info("Infrastructure Adapter: Finalizado el análisis. No se detectaron amenazas en las URLs enviadas.");
            return Collections.emptyList();
        }

        log.warn("Infrastructure Adapter: Se localizaron [{}] incidencias de riesgo potencial en los enlaces analizados.", response.getMatches().size());
        return response.getMatches().stream()
                .map(match -> String.format("- URL [%s] es amenaza: %s", match.getThreat().getUrl(), match.getThreatType()))
                .toList();
    }

    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class SafeBrowsingResponse {
        private List<Match> matches;
    }

    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Match {
        private Threat threat;
        @JsonProperty("threatType")
        private String threatType;
    }

    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Threat {
        private String url;
    }
}