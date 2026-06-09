package com.unlam.verabackend.infrastructure.provider;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.unlam.verabackend.domain.port.out.GeminiProvider;
import com.unlam.verabackend.domain.port.out.GeminiResult;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

@Component
public class GeminiAdapter implements GeminiProvider {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RestTemplate timedRestTemplate;

    @Value("${google.gemini.api-key}")
    private String apiKey;

    private static final String PRIMARY_MODEL = "gemini-3.5-flash";
    private static final String FALLBACK_MODEL = "gemini-2.5-flash";

    private static final Map<String, String> MIME_TYPE_MAP = Map.of(
            "mp3", "audio/mpeg",
            "jpg", "image/jpeg",
            "jpeg", "image/jpeg",
            "png", "image/png",
            "webp", "image/webp",
            "aac", "audio/aac",
            "flac", "audio/flac",
            "wav", "audio/wav",
            "mp4", "video/mp4"
    );

    public GeminiAdapter() {
        var factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(5000);
        factory.setReadTimeout(15000);
        this.timedRestTemplate = new RestTemplate(factory);
    }

    @Override
    public GeminiResult analyzeContent(String prompt, MultipartFile file) { // 🚀 Firma actualizada
        try {
            return executeWithModel(PRIMARY_MODEL, prompt, file);
        } catch (Exception e) {
            System.err.println("El modelo primario (" + PRIMARY_MODEL + ") falló. Intentando modelo fallback...");
            try {
                return executeWithModel(FALLBACK_MODEL, prompt, file);
            } catch (Exception ex) {
                System.err.println("ERROR CRÍTICO TOTAL: Ambos modelos de Gemini fallaron estrepitosamente.");
                throw new RuntimeException("Error crítico en el motor de análisis inteligente (Gemini). Detalle: " + ex.getMessage(), ex);
            }
        }
    }

    private GeminiResult executeWithModel(String model, String prompt, MultipartFile file) throws Exception {
        String url = "https://generativelanguage.googleapis.com/v1beta/models/" + model + ":generateContent?key=" + apiKey;

        Map<String, Object> requestBody = new HashMap<>();
        List<Map<String, Object>> contents = new ArrayList<>();
        Map<String, Object> contentMap = new HashMap<>();
        List<Map<String, Object>> parts = new ArrayList<>();

        Map<String, Object> textPart = new HashMap<>();
        textPart.put("text", prompt);
        parts.add(textPart);

        if (file != null && !file.isEmpty()) {
            Map<String, Object> inlineData = new HashMap<>();
            Map<String, Object> filePart = new HashMap<>();

            inlineData.put("mimeType", resolveMimeType(file));
            inlineData.put("data", Base64.getEncoder().encodeToString(file.getBytes()));

            filePart.put("inlineData", inlineData);
            parts.add(filePart);
        }

        contentMap.put("parts", parts);
        contents.add(contentMap);
        requestBody.put("contents", contents);

        Map<String, Object> generationConfig = new HashMap<>();
        generationConfig.put("responseMimeType", "application/json");
        requestBody.put("generationConfig", generationConfig);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<String> responseEntity = timedRestTemplate.postForEntity(url, entity, String.class);
            return mapToGeminiResult(responseEntity.getBody());
        } catch (HttpStatusCodeException e) {
            System.err.println("ERROR HTTP EN GOOGLE GEMINI (" + model + "): Código " + e.getStatusCode());
            System.err.println("RESPUESTA EXACTA DE GOOGLE: " + e.getResponseBodyAsString());
            throw e;
        }
    }

    private String resolveMimeType(MultipartFile file) {
        String filename = file.getOriginalFilename();
        if (filename == null || !filename.contains(".")) {
            return file.getContentType();
        }

        String extension = filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();

        if (MIME_TYPE_MAP.containsKey(extension)) {
            return MIME_TYPE_MAP.get(extension);
        }

        String contentType = file.getContentType();
        if (contentType == null || contentType.equals("application/octet-stream")) {
            if (Arrays.asList("heic", "heif").contains(extension)) return "image/" + extension;
            if (Arrays.asList("webm", "mov", "avi").contains(extension)) return "video/" + extension;
            return "audio/mpeg";
        }

        return contentType;
    }

    private GeminiResult mapToGeminiResult(String responseBody) throws Exception {
        var jsonNode = objectMapper.readTree(responseBody);

        String rawJsonResult = jsonNode.path("candidates").get(0)
                .path("content")
                .path("parts").get(0)
                .path("text").asText();

        return objectMapper.readValue(rawJsonResult, GeminiResult.class);
    }
}