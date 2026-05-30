package com.unlam.verabackend.infrastructure.provider;

import com.unlam.verabackend.domain.model.UrlValidation;
import com.unlam.verabackend.domain.ports.out.SafeBrowsingApiPort;
import com.unlam.verabackend.presentation.dto.request.SafeBrowsingApiRequest;
import com.unlam.verabackend.presentation.dto.response.SafeBrowsingApiResponse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import java.net.URI;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class SafeBrowsingApiProvider implements SafeBrowsingApiPort {

    private final RestTemplate restTemplate;
    private final String apiKey;

    public SafeBrowsingApiProvider(@Value("${google.safe-browsing.api-key:}") String apiKey) {
        this.restTemplate = new RestTemplate();
        this.apiKey = apiKey;
    }

    @Override
    public UrlValidation checkUrlsInContent(String content) {
        if (content == null || content.isBlank() || apiKey == null || apiKey.isBlank()) {
            return UrlValidation.empty();
        }

        List<String> extractedUrls = extractAllUrls(content);
        if (extractedUrls.isEmpty()) {
            return UrlValidation.empty();
        }

        try {
            String url = "https://safebrowsing.googleapis.com/v4/threatMatches:find?key=" + apiKey;

            SafeBrowsingApiRequest request = SafeBrowsingApiRequest.forUrls(extractedUrls);
            SafeBrowsingApiResponse response = restTemplate.postForObject(url, request, SafeBrowsingApiResponse.class);

            if (response == null || response.matches() == null || response.matches().isEmpty()) {
                return UrlValidation.empty();
            }

            List<String> threatTypes = response.matches().stream()
                    .map(SafeBrowsingApiResponse.ThreatMatch::threatType)
                    .filter(Objects::nonNull)
                    .distinct()
                    .toList();

            return new UrlValidation(true, threatTypes);
        } catch (Exception ex) {
            System.err.println("Error en proveedor Safe Browsing: " + ex.getMessage());
            return UrlValidation.empty();
        }
    }

    private List<String> extractAllUrls(String content) {
        String regex = "(?i)\\b((?:https?://|www\\d{0,3}\\.|[a-z0-9.\\-]+==/)(?:[^\\s()<>]+|\\(([^\\s()<>]+|(\\([^\\s()<>]+\\)))*\\))+(?:\\(([^\\s()<>]+|(\\([^\\s()<>]+\\)))*\\)|[^\\s`!()\\[\\]{};:'\".,<>?«» Rhodes“”指标‘’]))";
        Pattern urlPattern = Pattern.compile(regex);
        Matcher matcher = urlPattern.matcher(content);
        Set<String> uniqueUrls = new HashSet<>();

        while (matcher.find()) {
            String rawUrl = matcher.group();
            String candidate = rawUrl.toLowerCase().startsWith("www.") ? "http://" + rawUrl : rawUrl;
            try {
                URI uri = new URI(candidate);
                String scheme = uri.getScheme();
                if (scheme != null && (scheme.equalsIgnoreCase("http") || scheme.equalsIgnoreCase("https"))) {
                    uniqueUrls.add(uri.toASCIIString());
                }
            } catch (Exception e) {
                uniqueUrls.add(rawUrl);
            }
        }
        return uniqueUrls.stream().sorted().toList();
    }
}
