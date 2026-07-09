package com.unlam.verabackend.infrastructure.provider;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.unlam.verabackend.domain.model.ChatMessages;
import com.unlam.verabackend.domain.model.ChatsRole;
import com.unlam.verabackend.domain.port.out.AiProvider;
import com.unlam.verabackend.domain.port.out.AiResult;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.function.Function;

@Slf4j
@Component
@RequiredArgsConstructor
public class GeminiAiAdapter implements AiProvider {

    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate;

    @Value("${google.gemini.api-key}")
    private String apiKey;

    private static final String GEMINI_ENDPOINT_URL = "https://generativelanguage.googleapis.com/v1beta/models/{model}:generateContent?key={apiKey}";
    private static final String RESPONSE_MIME_JSON = "application/json";

    private static final List<String> MODELS_CASCADE = List.of(
            "gemini-2.5-flash-lite",
            "gemini-2.5-flash",
            "gemini-3.1-flash-lite",
            "gemini-3.5-flash",
            "gemini-3-flash-preview"
    );

    @Override
    @Retry(name = "geminiConfig")
    @CircuitBreaker(name = "geminiConfig")
    public AiResult analyzeContent(String prompt, MultipartFile file) {
        log.info("Infrastructure Adapter: Iniciando pipeline de análisis heurístico multifase.");
        return executeWithModelFailover(model -> requestAnalysisInference(model, prompt, file), "Análisis Estructurado");
    }

    @Override
    @Retry(name = "geminiConfig")
    @CircuitBreaker(name = "geminiConfig")
    public String generateChatResponse(String systemPrompt, List<ChatMessages> history) {
        log.info("Infrastructure Adapter: Solicitando respuesta conversacional contextualizada.");
        return executeWithModelFailover(model -> requestChatInference(model, systemPrompt, history), "Chat Conversacional");
    }

    private <T> T executeWithModelFailover(Function<String, T> modelAction, String contextName) {
        Exception lastException = null;

        for (String modelName : MODELS_CASCADE) {
            try {
                log.debug("Infrastructure Adapter: Despachando tarea [{}] al modelo de IA [{}]", contextName, modelName);
                return modelAction.apply(modelName);
            } catch (Exception e) {
                lastException = e;
                log.warn("Infrastructure Warning: El modelo [{}] reportó una anomalía en [{}]. Ejecutando salto de failover...", modelName, contextName);
            }
        }

        String actualError = (lastException != null) ? lastException.getMessage() : "Error desconocido de infraestructura";
        log.error("Infrastructure Critical: Agotados todos los modelos de contingencia para [{}]. Interrupción de servicio.", contextName);
        throw new RuntimeException("AI Provider Failure: El subsistema falló tras agotar el pool de respaldo. Detalle: " + actualError);
    }

    private AiResult requestAnalysisInference(String model, String prompt, MultipartFile file) {
        HttpEntity<Map<String, Object>> requestEntity = buildAnalysisPayload(prompt, file);
        Map<String, String> uriVariables = Map.of("model", model, "apiKey", apiKey);

        ResponseEntity<String> response = restTemplate.postForEntity(GEMINI_ENDPOINT_URL, requestEntity, String.class, uriVariables);

        try {
            String rawJsonResult = extractTextContentFromResponseBody(response.getBody());
            return objectMapper.readValue(rawJsonResult, AiResult.class);
        } catch (Exception e) {
            log.error("Infrastructure Mapping Error: La respuesta del modelo no pudo transformarse al contrato AiResult.");
            throw new IllegalStateException("Error al deserializar estructura JSON de análisis heurístico.", e);
        }
    }

    private String requestChatInference(String model, String systemPrompt, List<ChatMessages> history) {
        HttpEntity<Map<String, Object>> requestEntity = buildChatPayload(systemPrompt, history);
        Map<String, String> uriVariables = Map.of("model", model, "apiKey", apiKey);

        ResponseEntity<String> response = restTemplate.postForEntity(GEMINI_ENDPOINT_URL, requestEntity, String.class, uriVariables);

        try {
            return extractTextContentFromResponseBody(response.getBody());
        } catch (Exception e) {
            log.error("Infrastructure Mapping Error: Imposible parsear texto plano desde los nodos de la respuesta.");
            throw new IllegalStateException("Error al extraer texto conversacional de la respuesta de Gemini.", e);
        }
    }

    private HttpEntity<Map<String, Object>> buildAnalysisPayload(String prompt, MultipartFile file) {
        try {
            List<Map<String, Object>> parts = new ArrayList<>();
            parts.add(Map.of("text", prompt));

            if (file != null && !file.isEmpty()) {
                String encodedBase64Data = Base64.getEncoder().encodeToString(file.getBytes());
                parts.add(Map.of("inlineData", Map.of(
                        "mimeType", Objects.requireNonNull(file.getContentType()),
                        "data", encodedBase64Data
                )));
            }

            Map<String, Object> body = Map.of(
                    "contents", List.of(Map.of("parts", parts)),
                    "generationConfig", Map.of("responseMimeType", RESPONSE_MIME_JSON)
            );

            return new HttpEntity<>(body, createStandardHeaders());
        } catch (Exception e) {
            throw new IllegalArgumentException("Error al empaquetar binarios para el payload de análisis.", e);
        }
    }

    private HttpEntity<Map<String, Object>> buildChatPayload(String systemPrompt, List<ChatMessages> history) {
        Map<String, Object> systemInstruction = Map.of("parts", List.of(Map.of("text", systemPrompt)));

        List<Map<String, Object>> contents = history.stream()
                .map(msg -> Map.of(
                        "role", mapRoleToGeminiSpecification(msg.getRole()),
                        "parts", List.of(Map.of("text", msg.getContent()))
                )).toList();

        Map<String, Object> body = Map.of(
                "systemInstruction", systemInstruction,
                "contents", contents
        );

        return new HttpEntity<>(body, createStandardHeaders());
    }

    private String extractTextContentFromResponseBody(String jsonResponseBody) throws Exception {
        return objectMapper.readTree(jsonResponseBody)
                .path("candidates").get(0)
                .path("content").path("parts").get(0)
                .path("text").asText();
    }

    private String mapRoleToGeminiSpecification(ChatsRole domainRole) {
        return ChatsRole.MODEL.equals(domainRole) ? "model" : "user";
    }

    private HttpHeaders createStandardHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }
}