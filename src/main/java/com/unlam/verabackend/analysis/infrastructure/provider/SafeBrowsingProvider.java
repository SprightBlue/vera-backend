package com.unlam.verabackend.analysis.infrastructure.provider;

import com.unlam.verabackend.analysis.domain.ports.out.SafeBrowsingPort;
import com.unlam.verabackend.analysis.infrastructure.dto.SafeBrowsingRequest;
import com.unlam.verabackend.analysis.infrastructure.dto.SafeBrowsingResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Component
public class SafeBrowsingProvider implements SafeBrowsingPort {

    private static final String ENDPOINT_TEMPLATE = "https://safebrowsing.googleapis.com/v4/threatMatches:find?key=%s";

    private final RestTemplate restTemplate;
    private final String apiKey;

    public SafeBrowsingProvider(@Value("${google.safe-browsing.api-key:}") String apiKey) {
        this.restTemplate = new RestTemplate();
        this.apiKey = apiKey;
    }

    @Override
    public boolean checkUrl(String url) {
        if (url == null || url.isBlank()) {
            return false;
        }

        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("Google Safe Browsing API key is not configured");
        }

        try {
            SafeBrowsingResponse response = restTemplate.postForObject(
                    String.format(ENDPOINT_TEMPLATE, apiKey),
                    SafeBrowsingRequest.forUrl(url),
                    SafeBrowsingResponse.class
            );

            return response != null && response.getMatches() != null && !response.getMatches().isEmpty();
        } catch (RestClientException ex) {
            throw new IllegalStateException("Error calling Google Safe Browsing API", ex);
        }
    }
}

