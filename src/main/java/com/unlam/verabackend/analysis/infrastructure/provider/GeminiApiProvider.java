package com.unlam.verabackend.analysis.infrastructure.provider;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.unlam.verabackend.analysis.domain.ports.out.GeminiApiPort;
import com.unlam.verabackend.analysis.infrastructure.dto.GeminiApiRequest;
import com.unlam.verabackend.analysis.infrastructure.dto.GeminiApiResponse;
import com.unlam.verabackend.analysis.infrastructure.dto.GeminiDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class GeminiApiProvider implements GeminiApiPort {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final String apiKey;
    private final String model;
    private final String fallbackModel;

    public GeminiApiProvider(@Value("${google.gemini.api-key:}") String apiKey,
                             @Value("${google.gemini.model:gemini-2.5-flash}") String model,
                             @Value("${google.gemini.fallback-model:gemini-3.1-flash-lite}") String fallbackModel) {

        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(5000);
        requestFactory.setReadTimeout(20000);

        this.restTemplate = new RestTemplate(requestFactory);

        this.objectMapper = new ObjectMapper()
                .configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        this.apiKey = apiKey;
        this.model = model;
        this.fallbackModel = fallbackModel;
    }

    @Override
    public GeminiDto analyzeMessage(String prompt) {
        if (apiKey == null || apiKey.isBlank()) {
            System.err.println("API Key de Gemini no configurada. Usando fallback estático.");
            return GeminiDto.fallback();
        }

        try {
            return executeCall(prompt, this.model);
        } catch (Exception primaryEx) {
            System.err.println("Falló el modelo principal (" + this.model + "). Intentando modelo de fallback... Error: " + primaryEx.getMessage());

            if (fallbackModel != null && !fallbackModel.isBlank() && !fallbackModel.equals(model)) {
                try {
                    return executeCall(prompt, this.fallbackModel);
                } catch (Exception fallbackEx) {
                    System.err.println("También falló el modelo de fallback (" + this.fallbackModel + "). Error: " + fallbackEx.getMessage());
                }
            }

            return GeminiDto.fallback();
        }
    }

    private GeminiDto executeCall(String prompt, String modelName) throws Exception {
        String url = "https://generativelanguage.googleapis.com/v1beta/models/" + modelName + ":generateContent?key=" + apiKey;

        GeminiApiRequest request = GeminiApiRequest.forPrompt(prompt);
        GeminiApiResponse response = restTemplate.postForObject(url, request, GeminiApiResponse.class);

        if (response == null || response.getFirstText() == null) {
            throw new IllegalStateException("La respuesta de Gemini para " + modelName + " vino vacía");
        }

        return objectMapper.readValue(response.getFirstText(), GeminiDto.class);
    }
}
