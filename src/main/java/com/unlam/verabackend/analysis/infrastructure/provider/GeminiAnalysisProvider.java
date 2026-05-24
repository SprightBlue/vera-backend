package com.unlam.verabackend.analysis.infrastructure.provider;

import com.unlam.verabackend.analysis.domain.model.RiskLevel;
import com.unlam.verabackend.analysis.domain.ports.out.GeminiAnalysisPort;
import com.unlam.verabackend.analysis.infrastructure.dto.GeminiAnalysisRequest;
import com.unlam.verabackend.analysis.infrastructure.dto.GeminiAnalysisResponse;
import com.unlam.verabackend.analysis.infrastructure.dto.GeminiApiRequest;
import com.unlam.verabackend.analysis.infrastructure.dto.GeminiApiResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class GeminiAnalysisProvider implements GeminiAnalysisPort {

    private static final String ENDPOINT_TEMPLATE = "https://generativelanguage.googleapis.com/v1beta/models/%s:generateContent?key=%s";
    private static final Pattern BOOLEAN_FIELD_PATTERN = Pattern.compile("\"%s\"\\s*:\\s*(true|false)", Pattern.CASE_INSENSITIVE);
    private static final Pattern STRING_FIELD_PATTERN = Pattern.compile("\"%s\"\\s*:\\s*\"(.*?)\"", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

    private final RestTemplate restTemplate;
    private final String apiKey;
    private final String model;

    public GeminiAnalysisProvider(@Value("${google.gemini.api-key:}") String apiKey,
                                  @Value("${google.gemini.model:gemini-1.5-flash}") String model) {
        this.restTemplate = new RestTemplate();
        this.apiKey = apiKey;
        this.model = model;
    }

    @Override
    public GeminiAnalysisResponse analyzeMessage(GeminiAnalysisRequest request) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("Google Gemini API key is not configured");
        }

        String prompt = buildPrompt(request);

        try {
            GeminiApiResponse response = restTemplate.postForObject(
                    String.format(ENDPOINT_TEMPLATE, model, apiKey),
                    new GeminiApiRequest(prompt),
                    GeminiApiResponse.class
            );

            String jsonText = extractFirstText(response);
            return parseResponse(jsonText, request.isUrlMalicious());
        } catch (RestClientException ex) {
            throw new IllegalStateException("Error calling Gemini API", ex);
        }
    }

    private String buildPrompt(GeminiAnalysisRequest request) {
        return "Actua como analista de seguridad digital para adultos mayores. " +
                "Usa lenguaje simple, claro y cotidiano. Evita tecnicismos, siglas y palabras alarmistas. " +
                "Las recomendaciones deben ser tranquilas, respetuosas y concretas, sin generar miedo. " +
                "Los patrones sospechosos deben describirse con palabras faciles de entender, por ejemplo: 'te pide una clave', 'te apura para responder', 'parece un enlace raro'. " +
                "Devuelve SOLO JSON valido con este esquema exacto: " +
                "{\"isThreat\":boolean,\"riskLevel\":\"LOW|MEDIUM|HIGH\",\"suspiciousPatterns\":\"texto\",\"recommendation\":\"texto\"}. " +
                "No agregues markdown ni texto adicional. " +
                "Mensaje: " + request.getMessageContent() + "\n" +
                "Contiene URL: " + request.isHasUrl() + "\n" +
                "URL maliciosa segun Safe Browsing: " + request.isUrlMalicious() + "\n" +
                "Si la URL es maliciosa, isThreat debe ser true y riskLevel debe ser HIGH. " +
                "Si la URL no es maliciosa o no hay URL, define el analisis segun el contenido del mensaje.";
    }

    private String extractFirstText(GeminiApiResponse response) {
        if (response == null || response.getCandidates() == null || response.getCandidates().isEmpty()) {
            throw new IllegalStateException("Gemini returned an empty response");
        }
        GeminiApiResponse.Candidate candidate = response.getCandidates().getFirst();
        if (candidate.getContent() == null || candidate.getContent().getParts() == null || candidate.getContent().getParts().isEmpty()) {
            throw new IllegalStateException("Gemini returned response without text parts");
        }
        String text = candidate.getContent().getParts().getFirst().getText();
        if (text == null || text.isBlank()) {
            throw new IllegalStateException("Gemini returned blank text");
        }
        return text;
    }

    private GeminiAnalysisResponse parseResponse(String rawText, boolean urlMalicious) {
        String cleanedJson = extractJson(rawText);
        Boolean isThreat = extractBoolean(cleanedJson);
        String riskLevelText = extractString(cleanedJson, "riskLevel");
        String suspiciousPatterns = extractString(cleanedJson, "suspiciousPatterns");
        String recommendation = extractString(cleanedJson, "recommendation");

        RiskLevel riskLevel = parseRiskLevel(riskLevelText, urlMalicious);
        boolean threatValue = isThreat != null ? isThreat : urlMalicious;

        return new GeminiAnalysisResponse(
                threatValue,
                riskLevel,
                suspiciousPatterns != null ? suspiciousPatterns : "Hay detalles del mensaje que conviene revisar con calma.",
                recommendation != null ? recommendation : "Si te genera dudas, no respondas enseguida y consultalo con alguien de confianza."
        );
    }

    private String extractJson(String rawText) {
        int start = rawText.indexOf('{');
        int end = rawText.lastIndexOf('}');
        if (start >= 0 && end > start) {
            return rawText.substring(start, end + 1);
        }
        return rawText;
    }

    private Boolean extractBoolean(String json) {
        Matcher matcher = Pattern.compile(String.format(BOOLEAN_FIELD_PATTERN.pattern(), "isThreat"), Pattern.CASE_INSENSITIVE)
                .matcher(json);
        if (matcher.find()) {
            return Boolean.parseBoolean(matcher.group(1));
        }
        return null;
    }

    private String extractString(String json, String fieldName) {
        Matcher matcher = Pattern.compile(String.format(STRING_FIELD_PATTERN.pattern(), fieldName), Pattern.CASE_INSENSITIVE | Pattern.DOTALL)
                .matcher(json);
        if (matcher.find()) {
            return matcher.group(1).replace("\\n", "\n").replace("\\\"", "\"");
        }
        return null;
    }

    private RiskLevel parseRiskLevel(String value, boolean urlMalicious) {
        if (value != null) {
            try {
                return RiskLevel.valueOf(value.trim().toUpperCase());
            } catch (IllegalArgumentException ignored) {
                // Fall through to default.
            }
        }
        return urlMalicious ? RiskLevel.HIGH : RiskLevel.UNDEFINED;
    }
}

