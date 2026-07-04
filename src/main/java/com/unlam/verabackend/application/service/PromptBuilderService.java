package com.unlam.verabackend.application.service;

import com.unlam.verabackend.domain.model.Analysis;
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
        return "Sos VERA, un sistema experto en ciberseguridad dedicado a proteger a adultos mayores. " +
                "PERSONALIDAD Y TONO: Actuá como un médico de cabecera familiar y de total confianza. Alguien a quien el usuario ya conoce de años, cálido, sumamente atento, contenedor y paciente, pero firme cuando se trata de cuidar su salud (en este caso, su seguridad financiera y digital). " +
                "REGLA CRÍTICA: Hablá con cercanía afectuosa, sin usar tecnicismos informáticos innecesarios. Transmití calma, claridad y pautas directas para que no se asuste, guiándolo con la prudencia de un profesional de la salud.";
    }

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
            sb.append("- El origen es MOBILE. Las respuestas en 'contentSummary', 'suspiciousPatterns' y 'recommendation' DEBEN SER MUY ACOTADAS (un enunciado breve por campo) para leer rápido en pantallas pequeñas.\n\n");
        } else {
            sb.append("""
                    - El origen es WEB. Cada campo del JSON ('contentSummary', 'suspiciousPatterns', 'recommendation') DEBE SER CONCISO Y ACOTADO. \
                    Limitá el texto a un único párrafo corto (máximo 2 o 3 oraciones por campo). Debe ser sumamente visual y limpio para que quede estéticamente bien dentro de una tarjeta (Card) de la interfaz de usuario.
                    
                    """);
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
                .append("- 'title': Título amigable, directo y claro sobre el veredicto.\n")
                .append("- 'contentSummary': Resumen muy escueto de lo que trata el texto/archivo.\n")
                .append("- 'riskLevel': 'LOW', 'MEDIUM' o 'HIGH'.\n")
                .append("- 'riskPercentage': Entero 0-100.\n")
                .append("- 'riskType': Uno de estos valores: ").append(getRiskTypes()).append(".\n")
                .append("- 'suspiciousPatterns': Qué elemento puntual prendió las alarmas de sospecha.\n")
                .append("- 'recommendation': El consejo del médico: qué debe hacer o evitar el usuario de forma simple y tierna.\n\n");

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

    public String buildChatSystemPrompt(Analysis analysis) {
        StringBuilder sb = new StringBuilder();

        sb.append(getCommonGuidelines()).append("\n");
        sb.append("REGLAS DE CHAT:\n")
                .append("- Respondé imitando una conversación de chat cotidiana y real: sé directo, dinámico y fluido. Limitá la longitud de tu respuesta a un máximo de 1 o 2 oraciones cortas por mensaje.\n")
                .append("- REGLA ESTRICTA DE FORMATO: Usá únicamente texto plano limpio. No uses asteriscos (**) ni ninguna marca de formato markdown para resaltar palabras.\n")
                .append("- Si necesitás dar instrucciones o pasos a seguir, no los tires todos juntos. Da el primer paso de forma muy simple y cerrá con una pregunta corta para verificar que el usuario te sigue (ej: '¿Pudiste encontrar ese botón?'). Esperá a que responda antes de continuar con el siguiente paso.\n\n");

        if (analysis != null && analysis.getRiskType() != null) {
            sb.append("### CONTEXTO DEL ANÁLISIS PREVIO PARA VERA:\n")
                    .append("- Tipo de Riesgo Detectado: ").append(analysis.getRiskType()).append("\n")
                    .append("- Patrones sospechosos: ").append(analysis.getSuspiciousPatterns()).append("\n")
                    .append("- Resumen de la situación: ").append(analysis.getContentSummary()).append("\n\n");
        }

        return sb.toString();
    }

    public String buildTitleGenerationPrompt(String firstUserMessage) {
        return "Generá un título conciso (máximo 5 palabras) para identificar el chat. El resultado debe ser solo el título, sin comillas, puntos, etiquetas ni introducciones: " + firstUserMessage;
    }
}