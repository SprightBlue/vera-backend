package com.unlam.verabackend.infrastructure.provider;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.unlam.verabackend.domain.model.ChatMessages;
import com.unlam.verabackend.domain.port.out.AiProvider;
import com.unlam.verabackend.domain.port.out.AiResult;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.function.Function;

@Slf4j
@Component
public class GeminiAiProvider implements AiProvider {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RestTemplate timedRestTemplate;

    private static final List<String> MODELS = List.of(
            "gemini-3.5-flash",
            "gemini-3.1-flash-lite",
            "gemini-2.5-flash",
            "gemini-2.5-flash-lite"
    );

    @Value("${google.gemini.api-key}")
    private String apiKey;

    public GeminiAiProvider() {
        var factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(5000);
        factory.setReadTimeout(15000);
        this.timedRestTemplate = new RestTemplate(factory);
    }

    @Retry(name = "geminiConfig")
    @CircuitBreaker(name = "geminiConfig")
    @Override
    public AiResult analyzeContent(String prompt, MultipartFile file) {
        return executeWithFallback(model -> executeWithModel(model, prompt, file), "Análisis");
    }

    @Retry(name = "geminiConfig")
    @CircuitBreaker(name = "geminiConfig")
    @Override
    public String generateChatResponse(String systemPrompt, List<ChatMessages> history) {
        return executeWithFallback(model -> executeChatWithModel(model, systemPrompt, history), "Chat");
    }

    private <T> T executeWithFallback(Function<String, T> action, String context) {
        Exception lastException = null;
        for (String model : MODELS) {
            try {
                log.info("Intentando operación '{}' con modelo: {}", context, model);
                return action.apply(model);
            } catch (Exception e) {
                lastException = e;
                log.warn("Modelo {} falló en {}. Saltando al siguiente...", model, context);
            }
        }
        String errorMessage = (lastException != null) ? lastException.getMessage() : "Error desconocido";
        log.error("Todos los modelos fallaron para {}. Error final: {}", context, errorMessage);
        throw new RuntimeException("Error en AI Provider: " + context + " falló tras agotar todos los modelos de respaldo. Detalle: " + errorMessage);
    }

    private AiResult executeWithModel(String model, String prompt, MultipartFile file) {
        String url = "https://generativelanguage.googleapis.com/v1beta/models/" + model + ":generateContent?key=" + apiKey;
        HttpEntity<Map<String, Object>> entity = buildAnalysisRequest(prompt, file);
        ResponseEntity<String> response = timedRestTemplate.postForEntity(url, entity, String.class);
        try {
            return mapToGeminiResult(response.getBody());
        } catch (Exception e) {
            throw new RuntimeException("Error al mapear resultado de análisis: " + e.getMessage());
        }
    }

    private String executeChatWithModel(String model, String systemPrompt, List<ChatMessages> history) {
        String url = "https://generativelanguage.googleapis.com/v1beta/models/" + model + ":generateContent?key=" + apiKey;
        HttpEntity<Map<String, Object>> entity = buildChatRequest(systemPrompt, history);
        ResponseEntity<String> response = timedRestTemplate.postForEntity(url, entity, String.class);
        try {
            return extractTextFromResponse(response.getBody());
        } catch (Exception e) {
            throw new RuntimeException("Error al extraer texto de chat: " + e.getMessage());
        }
    }

    private HttpEntity<Map<String, Object>> buildAnalysisRequest(String prompt, MultipartFile file) {
        try {
            Map<String, Object> body = new HashMap<>();
            List<Map<String, Object>> parts = new ArrayList<>(List.of(Map.of("text", prompt)));
            if (file != null && !file.isEmpty()) {
                parts.add(Map.of("inlineData", Map.of("mimeType", Objects.requireNonNull(file.getContentType()),
                        "data", Base64.getEncoder().encodeToString(file.getBytes()))));
            }
            body.put("contents", List.of(Map.of("parts", parts)));
            body.put("generationConfig", Map.of("responseMimeType", "application/json"));
            return new HttpEntity<>(body, new HttpHeaders() {{ setContentType(MediaType.APPLICATION_JSON); }});
        } catch (Exception e) {
            throw new RuntimeException("Error construyendo request de análisis: " + e.getMessage());
        }
    }

    private HttpEntity<Map<String, Object>> buildChatRequest(String systemPrompt, List<ChatMessages> history) {
        Map<String, Object> body = new HashMap<>();
        body.put("systemInstruction", Map.of("parts", List.of(Map.of("text", systemPrompt))));
        body.put("contents", history.stream().map(msg -> Map.of("role", msg.getRole().name().toLowerCase(),
                "parts", List.of(Map.of("text", msg.getContent())))).toList());
        return new HttpEntity<>(body, new HttpHeaders() {{ setContentType(MediaType.APPLICATION_JSON); }});
    }

    private String extractTextFromResponse(String body) throws Exception {
        return objectMapper.readTree(body).path("candidates").get(0).path("content").path("parts").get(0).path("text").asText();
    }

    private AiResult mapToGeminiResult(String body) throws Exception {
        return objectMapper.readValue(extractTextFromResponse(body), AiResult.class);
    }
}