package com.unlam.verabackend.application.service;

import com.unlam.verabackend.domain.model.Analysis;
import com.unlam.verabackend.domain.model.Alerts;
import com.unlam.verabackend.domain.model.Source;
import com.unlam.verabackend.domain.model.RiskType;
import org.springframework.stereotype.Service;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PromptBuilderService {

    private String getRiskTypes() {
        return Arrays.stream(RiskType.values()).map(Enum::name).collect(Collectors.joining(", "));
    }

    private String getCommonGuidelines() {
        return "Sos VERA, un sistema experto en ciberseguridad dedicado a proteger a adultos mayores con nulo conocimiento técnico. " +
                "TONO: Profesional, serio pero sumamente protector, paciente y didáctico. " +
                "REGLA CRÍTICA: Debés explicar los riesgos como una autoridad confiable que prioriza la seguridad del usuario. No uses tecnicismos; explicá las amenazas con claridad, calma y prudencia, evitando tecnicismos informáticos innecesarios.";
    }

    // --- 1. PROMPT DE ANÁLISIS DE CONTENIDO (CORE) ---
    public String buildPrompt(List<String> safeBrowsingReport, String rawText, String fileText, Source source) {
        StringBuilder sb = new StringBuilder();

        sb.append(getCommonGuidelines()).append("\n\n");
        sb.append("Tu objetivo es analizar el contenido recibido para detectar fraudes, estafas o virus.\n");

        if ((rawText != null && !rawText.isBlank()) || (fileText != null && !fileText.isBlank())) {
            sb.append("### CONTENIDO A ANALIZAR:\n\"\"\"\n");
            if (rawText != null && !rawText.isBlank()) sb.append("[Texto]: ").append(rawText).append("\n");
            if (fileText != null && !fileText.isBlank()) sb.append("[Documento]: ").append(fileText).append("\n");
            sb.append("\"\"\"\n\n");
        }

        sb.append("### REGLA DE ESPACIO (ORIGEN: ").append(source.name()).append("):\n");
        if (Source.MOBILE.equals(source)) {
            sb.append("- El origen es MOBILE. Las respuestas en 'contentSummary', 'suspiciousPatterns' y 'recommendation' DEBEN SER MUY ACOTADAS y directas para leer en pantallas pequeñas.\n\n");
        } else {
            sb.append("- El origen es WEB. Las respuestas deben ser explicativas y detalladas, pero de una extensión máxima de 4 o 5 párrafos. No te excedas de ese límite.\n\n");
        }

        sb.append("### REPORTE SAFE BROWSING:\n");
        boolean hasThreats = safeBrowsingReport != null && !safeBrowsingReport.isEmpty();

        if (!hasThreats) {
            sb.append("- Las listas negras automatizadas no detectaron registros de amenazas en enlaces para este mensaje.\n\n");
        } else {
            sb.append("- Amenazas críticas reportadas directamente por el servidor:\n");
            for (String threat : safeBrowsingReport) sb.append("- ").append(threat).append("\n");
            sb.append("\n");
        }

        sb.append("### REGLAS DE GENERACIÓN DEL JSON:\n")
                .append("Devolvé OBLIGATORIAMENTE un JSON. REGLA ESTRICTA DE FORMATO: Todos los campos del JSON deben contener texto plano limpio. No utilices asteriscos (**) ni marcas de formato markdown para resaltar texto dentro de los valores.\n")
                .append("- 'title': Título amigable y claro sobre el veredicto.\n")
                .append("- 'contentSummary': Resumen detallado del texto/archivo analizado.\n")
                .append("- 'riskLevel': 'LOW', 'MEDIUM' o 'HIGH'.\n")
                .append("- 'riskPercentage': Entero 0-100.\n")
                .append("- 'riskType': Uno de estos valores: ").append(getRiskTypes()).append(".\n")
                .append("- 'suspiciousPatterns': Elementos específicos que generaron sospecha.\n")
                .append("- 'recommendation': Pasos claros, tiernos y prácticos sobre qué hacer o evitar.\n\n");

        if (hasThreats) {
            sb.append("  * REGLA: Al existir una amenaza explícita en las listas negras, el veredicto obligatoriamente es crítico: riskLevel=HIGH, riskPercentage > 80, y riskType != NONE.\n");
        } else {
            sb.append("  * REGLA DE SEGURIDAD ESTRICTA PARA EVITAR FALSOS NEGATIVOS:\n")
                    .append("    - NOTA CRÍTICA: Que el reporte automatizado esté limpio NO implica que el contenido sea seguro. Los fraudes dirigidos usan URLs nuevas que evaden los sistemas tradicionales de filtrado.\n")
                    .append("    - Es obligatorio que realices un análisis heurístico exhaustivo de la redacción del mensaje y la estructura semántica de los enlaces.\n")
                    .append("    - Asigná 'LOW' por defecto únicamente si el contenido es verificado, legítimo y completamente seguro.\n")
                    .append("    - Asigná 'MEDIUM' si detectás indicadores dudosos o solicitudes inusuales que posean sospechas reales y fundamentadas.\n")
                    .append("    - Asigná 'HIGH' si tu análisis contextual confirma que se trata de un intento de phishing o estafa activa, incluso si el reporte automatizado de enlaces dio limpio. Priorizá la seguridad sin emitir falsas alarmas al cuidador por tonterías.");
        }

        sb.append("\n\nIMPORTANTE: Respondé ÚNICAMENTE con el JSON puro, sin markdown ni bloques de código. Empezá con { y terminá con }.");
        return sb.toString();
    }

    // --- 2. CONSTRUCTOR DEL SYSTEM PROMPT PARA CHATS ---
    public String buildChatSystemPrompt(Analysis analysis, Alerts alert) {
        StringBuilder sb = new StringBuilder();

        sb.append(getCommonGuidelines()).append("\n");
        sb.append("REGLAS DE CHAT:\n")
                .append("- Respondé de forma directa y profesional, limitándote a 3-4 oraciones claras.\n")
                .append("- REGLA ESTRICTA DE FORMATO: Usá únicamente texto plano limpio. No uses asteriscos (**) ni ninguna marca de formato markdown para resaltar palabras.\n")
                .append("- Si el procedimiento requiere varios pasos, preséntalos de forma secuencial y sencilla, finalizando siempre con una pregunta de verificación para asegurar que el usuario comprenda la instrucción.\n\n");

        if (analysis != null || alert != null) {
            sb.append("### CONTEXTO PARA VERA:\n")
                    .append("- Riesgo: ").append(analysis != null ? analysis.getRiskType() : alert.getRiskType()).append("\n")
                    .append("- Patrones detectados: ").append(analysis != null ? analysis.getSuspiciousPatterns() : alert.getSuspiciousPatterns()).append("\n")
                    .append("- Resumen: ").append(analysis != null ? analysis.getContentSummary() : alert.getContentSummary()).append("\n\n");
        }

        return sb.toString();
    }

    // --- 3. CONSTRUCTOR PARA GENERAR TÍTULOS ---
    public String buildTitleGenerationPrompt(String firstUserMessage) {
        return "Generá un título conciso (máximo 5 palabras) para identificar el chat. El resultado debe ser solo el título, sin comillas, puntos, etiquetas ni introducciones: " + firstUserMessage;
    }
}