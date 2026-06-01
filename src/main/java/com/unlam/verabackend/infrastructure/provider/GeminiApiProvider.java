package com.unlam.verabackend.infrastructure.provider;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.unlam.verabackend.domain.model.UrlValidation;
import com.unlam.verabackend.domain.model.MessageAssessment;
import com.unlam.verabackend.domain.ports.out.GeminiApiPort;
import com.unlam.verabackend.infrastructure.dto.GeminiApiRequest;
import com.unlam.verabackend.infrastructure.dto.GeminiApiResponse;
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
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);

        this.apiKey = apiKey;
        this.model = model;
        this.fallbackModel = fallbackModel;
    }

    @Override
    public MessageAssessment analyzeMessageContent(String content, UrlValidation urlValidation) {
        if (apiKey == null || apiKey.isBlank()) {
            return getFallbackPrediction();
        }

        String finalPrompt = buildMobileOptimizedPrompt(content, urlValidation);

        try {
            return executeCall(finalPrompt, this.model);
        } catch (Exception primaryEx) {
            System.err.println("Falló el modelo principal (" + this.model + "). Reintentando fallback... Error: " + primaryEx.getMessage());

            if (fallbackModel != null && !fallbackModel.isBlank() && !fallbackModel.equals(model)) {
                try {
                    return executeCall(finalPrompt, this.fallbackModel);
                } catch (Exception fallbackEx) {
                    System.err.println("Falló el modelo de fallback (" + this.fallbackModel + "). Error: " + fallbackEx.getMessage());
                }
            }
            return getFallbackPrediction();
        }
    }

    private MessageAssessment executeCall(String prompt, String modelName) throws Exception {
        String url = "https://generativelanguage.googleapis.com/v1beta/models/" + modelName + ":generateContent?key=" + apiKey;

        GeminiApiRequest request = GeminiApiRequest.forPrompt(prompt);
        GeminiApiResponse response = restTemplate.postForObject(url, request, GeminiApiResponse.class);

        if (response == null || response.getFirstText() == null) {
            throw new IllegalStateException("La respuesta de Gemini vino vacía");
        }

        return objectMapper.readValue(response.getFirstText(), MessageAssessment.class);
    }

    private MessageAssessment getFallbackPrediction() {
        return new MessageAssessment(
                "UNDEFINED",
                "Hay detalles del mensaje que conviene revisar con calma.",
                "Si te genera dudas, no respondas enseguida y consúltalo con alguien de confianza."
        );
    }

    private String buildMobileOptimizedPrompt(String content, UrlValidation result) {

        return "# ROL\n" +
                "Actúa como un analista experto en seguridad digital especializado en la protección de adultos mayores.\n\n" +
                "# OBJETIVO\n" +
                "Analiza el mensaje proporcionado para determinar si es un intento de estafa, fraude, phishing o suplantación de identidad.\n\n" +
                "# REGLAS CRÍTICAS DE REDACCIÓN PARA PANTALLAS MÓVILES (MOBILE UI)\n" +
                "- Sé extremadamente sintético, directo y breve.\n" +
                "- El texto debe diseñarse para ser leído en pantallas pequeñas de celulares sin cansar al usuario.\n" +
                "- En 'suspiciousPatterns', describe lo que huele mal en una o dos oraciones cortas (máximo 120 caracteres). No uses listas largas.\n" +
                "- En 'recommendation', ofrece un consejo afectuoso, claro y de acción inmediata en una oración simple (máximo 100 caracteres).\n" +
                "- Evita por completo explicaciones técnicas, palabras complejas o tecnicismos informáticos.\n\n" +
                "# REGLAS DE EVALUACIÓN DE RIESGO\n" +
                "1. Si 'Resultado de Safe Browsing' indica 'Malicious: true', el campo 'riskLevel' DEBE ser estrictamente \"HIGH\".\n" +
                "2. Si es false, define 'riskLevel' (LOW, MEDIUM, HIGH) basándote exclusivamente en la semántica e intención del texto.\n\n" +
                "# DATOS DE ENTRADA\n" +
                "- MENSAJE DEL USUARIO: \"\"\"" + (content == null ? "" : content) + "\"\"\"\n" +
                "- RESULTADO DE SAFE BROWSING:\n" +
                "  * Malicious: " + result.malicious() + "\n" +
                "  * Categorías de Amenazas: " + (result.threatTypes().isEmpty() ? "Ninguna" : String.join(", ", result.threatTypes())) + "\n\n" +
                "# FORMATO DE SALIDA\n" +
                "Devuelve ÚNICAMENTE un objeto JSON válido sin bloques markdown (```json).\n" +
                "{\n" +
                "  \"risk_level\": \"LOW\" | \"MEDIUM\" | \"HIGH\",\n" +
                "  \"suspicious_patterns\": \"Breve frase legible en celular.\",\n" +
                "  \"recommendation\": \"Consejo corto directo.\"\n" +
                "}";
    }
}
