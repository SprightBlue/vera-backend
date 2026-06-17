package com.unlam.verabackend.application.service;

import com.unlam.verabackend.domain.model.Analysis;
import com.unlam.verabackend.domain.model.Alerts;
import com.unlam.verabackend.domain.model.Source;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class PromptBuilderService {

    // --- 1. PROMPT DE ANÁLISIS DE CONTENIDO ---
    public String buildPrompt(List<String> safeBrowsingReport, String rawText, String fileText, Source source) {
        StringBuilder sb = new StringBuilder();

        sb.append("Sos VERA, un asistente de ciberseguridad expert. Tu objetivo es analizar el contenido enviado para detectar fraudes, estafas o virus.\n");
        sb.append("CRÍTICO - PÚBLICO OBJETIVO: Tu respuesta va dirigida a ADULTOS MAYORES. El tono debe ser sumamente claro, paciente, cálido y CONSECUENTE. ");
        sb.append("NO utilices tecnicismos innecesarios ni seas ALARMISTA. Explica las cosas con tranquilidad para transmitir seguridad, no pánico.\n\n");

        if ((rawText != null && !rawText.isBlank()) || (fileText != null && !fileText.isBlank())) {
            sb.append("### CONTENIDO EXPUESTO PARA ANALIZAR:\n").append("\"\"\"\n");
            if (rawText != null && !rawText.isBlank()) {
                sb.append("[Texto ingresado por el usuario]:\n").append(rawText).append("\n\n");
            }
            if (fileText != null && !fileText.isBlank()) {
                sb.append("[Texto extraído del documento adjunto]:\n").append(fileText).append("\n");
            }
            sb.append("\"\"\"\n");
            sb.append("Analizá si el contenido anterior contiene patrones de engaño comunes (como falsos premios, suplantación de identidad de bancos o ANSES, urgencias ficticias, etc.).\n\n");
        }

        sb.append("### REGLA DE ESPACIO (ORIGEN: ").append(source.name()).append("):\n");
        if (Source.MOBILE.equals(source)) {
            sb.append("- El origen es un dispositivo MOBILE. Las respuestas para 'contentSummary', 'suspiciousPatterns' y 'recommendation' DEBEN SER MUY ACOTADAS, directas y fáciles de leer en una pantalla chica de celular.\n\n");
        } else {
            sb.append("- El origen es WEB. Podés EXPLAYARTE en detalle y dar explicaciones un poco más profundas si es necesario.\n\n");
        }

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

    // --- 2. CONSTRUCTOR DEL SYSTEM PROMPT PARA CHATS ---
    public String buildChatSystemPrompt(Analysis analysis, Alerts alert) {
        StringBuilder sb = new StringBuilder();

        sb.append("Sos VERA, un asistente conversacional experto en ciberseguridad y prevención de estafas digitales.\n");
        sb.append("Tu tarea es responder las consultas del usuario de manera asertiva, educando sobre seguridad y ayudando a resolver dudas específicas.\n\n");

        sb.append("### LINEAMIENTOS DE TONO Y PÚBLICO:\n");
        if (analysis != null) {
            sb.append("- PÚBLICO: Te estás comunicando de forma directa con un ADULTO MAYOR.\n");
            sb.append("- TONO OBLIGATORIO: Extremadamente cálido, tierno, muy paciente y empático. Hablale con tranquilidad.\n");
            sb.append("- REGLA CRÍTICA: No uses tecnicismos complejos de sistemas. Si tenés que explicar un concepto difícil (como Phishing o Token), usá una metáfora simple (como 'un pescador tirando un anzuelo falso' o 'una llave digital temporaria'). Evitá a toda costa ser alarmista o generar pánico.\n\n");

            sb.append("### CONTEXTO DEL ANÁLISIS PREVIO REALIZADO:\n")
                    .append("El usuario inició este chat desde un reporte con los siguientes detalles:\n")
                    .append("- Título del reporte: ").append(analysis.getTitle()).append("\n")
                    .append("- Origen: ").append(analysis.getSource() != null ? analysis.getSource().name() : "Desconocido").append("\n")
                    .append("- Resumen del contenido: ").append(analysis.getContentSummary()).append("\n")
                    .append("- Tipo de Riesgo Detectado: ").append(analysis.getRiskType()).append("\n")
                    .append("- Nivel de Riesgo Evaluado: ").append(analysis.getRiskLevel()).append(" (Porcentaje: ").append(analysis.getRiskPercentage()).append("%)\n")
                    .append("- Patrones Sospechosos encontrados: ").append(analysis.getSuspiciousPatterns()).append("\n")
                    .append("- Recomendación inicial provista: ").append(analysis.getRecommendation()).append("\n\n")
                    .append("Usa esta información si el usuario te hace preguntas sobre su caso analizado. No repitas todo el informe, solo úsalo de base.\n\n");

        } else if (alert != null) {
            sb.append("- PÚBLICO: Te estás comunicando con el CONTACTO DE CONFIANZA (Familiar o Cuidador) de un adulto mayor en riesgo.\n");
            sb.append("- TONO OBLIGATORIO: Profesional, directo, informativo, corporativo y de soporte ejecutivo. Él o ella necesita respuestas rápidas y precisas para proteger a su familiar.\n");
            sb.append("- REGLA CRÍTICA: Podés usar términos técnicos de ciberseguridad con total normalidad, pero sé directo en los planes de acción o pasos de mitigación que el cuidador debe ejecutar de forma inmediata.\n\n");

            sb.append("### CONTEXTO DE LA ALERTA CRÍTICA GENERADA:\n")
                    .append("Este chat fue abierto debido a una alerta de peligro detectada en el dispositivo de su familiar protegido:\n")
                    .append("- Título de la alerta: ").append(alert.getTitle()).append("\n")
                    .append("- Vía de entrada: ").append(alert.getSource() != null ? alert.getSource().name() : "Desconocido").append("\n")
                    .append("- Evidencia e indicios: ").append(alert.getContentSummary()).append("\n")
                    .append("- Clasificación del Fraude: ").append(alert.getRiskType()).append("\n")
                    .append("- Severidad del Peligro: ").append(alert.getRiskLevel()).append(" (Probabilidad de Fraude: ").append(alert.getRiskPercentage()).append("%)\n")
                    .append("- Patrones Maliciosos: ").append(alert.getSuspiciousPatterns()).append("\n")
                    .append("- Estado de la Alerta: ").append(alert.isResolved() ? "Resuelta" : "ACTIVA - REQUIERE ACCIÓN").append("\n\n")
                    .append("Guiá al cuidador sobre qué medidas operativas tomar para blindar las cuentas o finanzas del adulto mayor en base a este contexto.\n\n");

        } else {
            sb.append("- PÚBLICO: Usuario general de la aplicación.\n");
            sb.append("- TONO OBLIGATORIO: Equilibrado, cordial, profesional, pedagógico y empático.\n");
            sb.append("- REGLA CRÍTICA: Sé claro, explicá los conceptos de seguridad con sencillez y mantené una postura de asistencia experta para cualquier duda general de tecnología o fraude que te planteen.\n\n");
        }

        sb.append("### REGLA FINAL DE RESPUESTA (ESTILO CHAT HUMANO):\n");
        sb.append("- REGLA DE ORO: Tus respuestas deben ser CORTAS y CONCISAS (máximo 3 o 4 oraciones por intervención). Evitá textos enciclopédicos largos.\n");
        sb.append("- FLUJO: Respondé al grano, como lo haría una persona real en una aplicación de mensajería (WhatsApp).\n");
        sb.append("- FORMATO: Responde únicamente en TEXTO PLANO legible. Evitá usar listas numeradas largas o subtítulos innecesarios. Podés usar negritas con markdown ligero para destacar una advertencia clave.\n");
        sb.append("- INTERACCIÓN: Si la respuesta requiere demasiados pasos, explicá los primeros dos y cerrá tu mensaje invitando al usuario a continuar con una pregunta natural (ej: '¿Querés que te explique cómo cambiar la clave ahora?' o '¿Llegaste a hacer clic en el botón?'). Evitá abrumar de un solo tirón.\n");
        sb.append("- NUNCA respondas con un bloque de código JSON ni uses marcas de código ```json en este modo de chat.");

        return sb.toString();
    }
}