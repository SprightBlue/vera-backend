package com.unlam.verabackend.analysis.infrastructure.provider;

import com.unlam.verabackend.analysis.domain.ports.out.SafeBrowsingApiPort;
import com.unlam.verabackend.analysis.infrastructure.dto.SafeBrowsingApiRequest;
import com.unlam.verabackend.analysis.infrastructure.dto.SafeBrowsingApiResponse;
import com.unlam.verabackend.analysis.infrastructure.dto.SafeBrowsingDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Objects;

@Component
public class SafeBrowsingApiProvider implements SafeBrowsingApiPort {

    private final RestTemplate restTemplate;
    private final String apiKey;

    public SafeBrowsingApiProvider(@Value("${google.safe-browsing.api-key:}") String apiKey) {
        this.restTemplate = new RestTemplate();
        this.apiKey = apiKey;
    }

    @Override
    public SafeBrowsingDto checkUrls(List<String> urls) {
        if (urls == null || urls.isEmpty() || apiKey == null || apiKey.isBlank()) {
            return SafeBrowsingDto.empty();
        }

        try {
            String url = "https://safebrowsing.googleapis.com/v4/threatMatches:find?key=" + apiKey;

            SafeBrowsingApiRequest request = SafeBrowsingApiRequest.forUrls(urls);
            SafeBrowsingApiResponse response = restTemplate.postForObject(url, request, SafeBrowsingApiResponse.class);

            if (response == null || response.matches() == null || response.matches().isEmpty()) {
                return SafeBrowsingDto.empty();
            }

            int matchCount = response.matches().size();

            List<String> threatTypes = response.matches().stream()
                    .map(SafeBrowsingApiResponse.ThreatMatch::threatType)
                    .filter(Objects::nonNull)
                    .distinct()
                    .toList();

            List<String> matchedUrls = response.matches().stream()
                    .map(SafeBrowsingApiResponse.ThreatMatch::threat)
                    .filter(Objects::nonNull)
                    .map(SafeBrowsingApiResponse.ThreatEntry::url)
                    .filter(Objects::nonNull)
                    .distinct()
                    .toList();

            return new SafeBrowsingDto(true, matchCount, threatTypes, matchedUrls);
        } catch (Exception ex) {
            System.err.println("Error en proveedor Safe Browsing. Continuando sin verificación de URL. Detalle: " + ex.getMessage());
            return SafeBrowsingDto.empty();
        }
    }
}
