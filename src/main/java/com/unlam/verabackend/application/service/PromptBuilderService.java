package com.unlam.verabackend.application.service;

import com.unlam.verabackend.domain.model.Source;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class PromptBuilderService {

    public String buildPrompt(List<String> safeBrowsingReport, String rawText, String fileText, Source source) {
        StringBuilder sb = new StringBuilder();

        // 1. Rol, Público y Tono Amigable
        sb.append("Sos VERA, un asistente de ciberseguridad experto. Tu objetivo es analizar el contenido enviado para detectar fraudes, estafas o virus.\n");
        sb.append("CRÍTICO - PÚBLICO OBJETIVO: Tu respuesta va dirigida a ADULTOS MAYORES. El tono debe ser sumamente claro, paciente, cálido y CONSECUENTE. ");
        sb.append("NO utilices tecnicismos innecesarios ni seas ALARMISTA. Explica las cosas con tranquilidad para transmitir seguridad, no pánico.\n\n");

        // 2. Inyección de Contenidos
        if ((rawText != null && !rawText.isBlank()) || (fileText != null && !fileText.isBlank())) {
            sb.append("### CONTENIDO EXPUESTO PARA ANALIZAR:\n")
                    .append("\"\"\"\n");

            if (rawText != null && !rawText.isBlank()) {
                sb.append("[Texto ingresado por el usuario]:\n").append(rawText).append("\n\n");
            }

            if (fileText != null && !fileText.isBlank()) {
                sb.append("[Texto extraído del documento adjunto]:\n").append(fileText).append("\n");
            }

            sb.append("\"\"\"\n");
            sb.append("Analizá si el contenido anterior contiene patrones de engaño comunes (como falsos premios, suplantación de identidad de bancos o ANSES, urgencias ficticias, etc.).\n\n");
        }

        // 3. Reglas de Espacio según el origen
        sb.append("### REGLA DE ESPACIO (ORIGEN: ").append(source.name()).append("):\n");
        if (Source.MOBILE.equals(source)) {
            sb.append("- El origen es un dispositivo MOBILE. Las respuestas para 'contentSummary', 'suspiciousPatterns' y 'recommendation' DEBEN SER MUY ACOTADAS, directas y fáciles de leer en una pantalla chica de celular.\n\n");
        } else {
            sb.append("- El origen es WEB. Podés EXPLAYARTE en detalle y dar explicaciones un poco más profundas si es necesario.\n\n");
        }

        // 4. Reporte Técnico de Google Safe Browsing
        sb.append("### REPORTE TÉCNICO DE ENLACES (GOOGLE SAFE BROWSING):\n");
        boolean hasThreats = safeBrowsingReport != null && !safeBrowsingReport.isEmpty();

        if (!hasThreats) {
            sb.append("- No se detectaron amenazas externas en los enlaces analizados por el servidor.\n\n");
        } else {
            sb.append("- El análisis de enlaces arrojó novedades riesgosas y URLs peligrosas encontradas en el mensaje:\n");
            for (String threat : safeBrowsingReport) {
                sb.append("- ").append(threat).append("\n");
            }
            sb.append("\n");
        }

        // 5. Reglas de Formato Estricto JSON y Forzado de Riesgo
        sb.append("### REGLAS DE GENERACIÓN DEL JSON:\n");
        sb.append("Debes devolver OBLIGATORIAMENTE un objeto JSON con los siguientes campos estrictos:\n")
                .append("- 'title': Un título amigable y muy claro sobre el veredicto (ej: 'Mensaje Seguro' o 'Recomendamos precaución').\n")
                .append("- 'contentSummary': Resumen de qué trata el texto o archivo analizado.\n");

        sb.append("- 'riskLevel': Nivel de riesgo en MAYÚSCULAS. Debes elegir estrictamente uno de los siguientes valores permitidos:\n")
                .append("  * 'LOW': Si el contenido es legítimo, seguro y no representa ninguna amenaza.\n")
                .append("  * 'MEDIUM': Si presenta elementos dudosos, remitentes extraños o solicita acciones poco habituales.\n")
                .append("  * 'HIGH': Si es un fraude confirmado, virus, robo de datos evidente o trampa financiera.\n");

        if (hasThreats) {
            sb.append("  * REGLA DE NEGOCIO OBLIGATORIA: Como Google Safe Browsing detectó enlaces maliciosos activos, el 'riskLevel' DEBE SER SÍ O SÍ 'HIGH' (en mayúsculas) y el 'riskPercentage' mayor a 80%.\n");
        } else {
            sb.append("  * Si Safe Browsing está limpio, determiná vos el 'riskLevel' analizando el texto o el archivo adjunto de forma contextual.\n");
        }

        sb.append("- 'riskPercentage': Un entero de 0 a 100.\n");

        sb.append("- 'riskType': Clasificación del tipo de fraude detectado en MAYÚSCULAS. Debes elegir estrictamente uno de los siguientes valores permitidos:\n")
                .append("  * 'NONE': Si el riesgo es totalmente seguro o bajo sin indicios de fraude.\n")
                .append("  * 'PHISHING': Páginas web o correos falsos que simulan ser marcas/entidades legítimas.\n")
                .append("  * 'SMISHING': Mensajes de texto (SMS) maliciosos con enlaces engañosos.\n")
                .append("  * 'VISHING': Audios, llamadas sospechosas o simulaciones telefónicas.\n")
                .append("  * 'FINANCIAL_FRAUD': Comprobantes falsos, requerimientos de dinero express o estafas bancarias.\n")
                .append("  * 'IDENTITY_THEFT': Intentos de hackeo de WhatsApp, pedidos de contraseñas o datos de DNI.\n")
                .append("  * 'MALWARE_LINK': Enlaces directos a virus, troyanos o descargas automáticas de APKs.\n");

        if (hasThreats) {
            sb.append("  * REGLA DE NEGOCIO OBLIGATORIA: Si Google Safe Browsing reportó amenazas, el 'riskType' NO puede ser 'NONE'. Clasifícalo según el contexto como 'PHISHING' o 'MALWARE_LINK'.\n");
        }

        sb.append("- 'suspiciousPatterns': Qué elementos te llamaron la atención del texto/archivo (o indicar que todo luce normal de forma amable).\n")
                .append("- 'recommendation': Consejos prácticos e instrucciones tiernas y comprensibles sobre qué hacer (o no hacer) a continuación.\n\n");

        sb.append("IMPORTANTE: Responde ÚNICAMENTE con el objeto JSON puro, sin decoraciones de código markdown (no uses las marcas ```json) ni textos adicionales fuera del JSON. Debe empezar con { y terminar con }.");

        return sb.toString();
    }
}