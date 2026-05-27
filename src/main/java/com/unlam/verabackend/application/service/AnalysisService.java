package com.unlam.verabackend.application.service;

import com.unlam.verabackend.domain.model.Analysis;
import com.unlam.verabackend.domain.model.Message;
import com.unlam.verabackend.domain.model.RiskAlert;
import com.unlam.verabackend.domain.model.RiskLevel;
import com.unlam.verabackend.infrastructure.dto.GeminiDto;
import com.unlam.verabackend.infrastructure.dto.SafeBrowsingDto;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class AnalysisService {

    public List<String> extractAllUrls(String content) {
        if (content == null || content.isBlank()) return List.of();

        String regex = "(?i)\\b((?:https?://|www\\d{0,3}\\.|[a-z0-9.\\-]+==/)(?:[^\\s()<>]+|\\(([^\\s()<>]+|(\\([^\\s()<>]+\\)))*\\))+(?:\\(([^\\s()<>]+|(\\([^\\s()<>]+\\)))*\\)|[^\\s`!()\\[\\]{};:'\".,<>?«»“”‘’]))";
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

    public String buildPrompt(String content, List<String> urls, SafeBrowsingDto safeBrowsingDto) {
        StringBuilder sb = new StringBuilder();

        sb.append("# ROL\n")
                .append("Actúa como un analista experto en seguridad digital especializado en la protección de adultos mayores.\n\n")

                .append("# OBJETIVO\n")
                .append("Analiza el mensaje proporcionado para determinar si es un intento de estafa, fraude, phishing o suplantación de identidad. ")
                .append("Genera una respuesta comprensiva, empática y libre de pánico.\n\n")

                .append("# PAUTAS DE REDACCIÓN (CRÍTICO)\n")
                .append("- Usa un lenguaje simple, cercano, cotidiano y respetuoso.\n")
                .append("- Evita por completo tecnicismos, siglas complejas (como 'phishing', 'malware') y palabras alarmistas.\n")
                .append("- En 'suspiciousPatterns', describe los ganchos maliciosos de forma sencilla (ej: 'Te apuran para responder', 'Se hacen pasar por un banco', 'El enlace es extraño').\n")
                .append("- En 'recommendation', ofrece consejos prácticos, calmados y directos (ej: 'No abras el enlace', 'Habla con un familiar antes de responder').\n\n")

                .append("# REGLAS DE EVALUACIÓN DE RIESGO\n")
                .append("1. Si 'Resultado de Safe Browsing' indica 'MALICIOUS: TRUE', el campo 'riskLevel' DEBE ser estrictamente \"HIGH\".\n")
                .append("2. Si Safe Browsing está limpio o no hay URLs, define 'riskLevel' (LOW, MEDIUM, HIGH) basándote exclusivamente en el análisis semántico del texto (si denota urgencia, pide transferencias, solicita datos sensibles, etc.).\n\n")

                .append("# DATOS DE ENTRADA\n")
                .append("- MENSAJE DEL USUARIO: \"\"\"").append(content == null ? "" : content).append("\"\"\"\n")
                .append("- URLS DETECTADAS EN EL MENSAJE: ").append(urls == null || urls.isEmpty() ? "Ninguna" : String.join(", ", urls)).append("\n");

        SafeBrowsingDto dto = safeBrowsingDto == null ? SafeBrowsingDto.empty() : safeBrowsingDto;
        sb.append("- RESULTADO DE SAFE BROWSING: [Malicious: ").append(dto.malicious())
                .append(", Coincidencias: ").append(dto.matchCount())
                .append(", Amenazas: ").append(String.join("-", dto.threatTypes())).append("]\n\n")

                .append("# FORMATO DE SALIDA\n")
                .append("Debes devolver ÚNICAMENTE un objeto JSON válido. No incluyas explicaciones fuera de él, ni marcas de código como ```json.\n")
                .append("{\n")
                .append("  \"riskLevel\": \"LOW\" | \"MEDIUM\" | \"HIGH\",\n")
                .append("  \"suspiciousPatterns\": \"Explicación simple en español de lo que huele mal\",\n")
                .append("  \"recommendation\": \"Consejo afectuoso y claro de qué hacer\"\n")
                .append("}");

        return sb.toString();
    }

    public Analysis buildAnalysis(Message message, GeminiDto geminiDto) {
        return new Analysis(
                UUID.randomUUID(),
                message.getId(),
                RiskLevel.fromString(geminiDto.riskLevel()),
                geminiDto.suspiciousPatterns(),
                geminiDto.recommendation(),
                LocalDateTime.now()
        );
    }

    public RiskAlert buildAlert(UUID analysisId, Long caregiverId) {
        return new RiskAlert(
                UUID.randomUUID(),
                analysisId,
                caregiverId,
                false,
                LocalDateTime.now()
        );
    }

}
