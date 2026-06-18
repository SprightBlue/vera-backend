package com.unlam.verabackend.infrastructure.provider;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import com.unlam.verabackend.infrastructure.entity.AlertsEntity;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.MimeMessageHelper;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String remitente;

    @Async
    public void enviarEmailPrueba(String destinatario) {
        try {
            SimpleMailMessage mensaje = new SimpleMailMessage();
            mensaje.setFrom(remitente);
            mensaje.setTo(destinatario);
            mensaje.setSubject("¡Hola desde Vera! 🛡️");
            mensaje.setText("Si estás leyendo esto, la configuración de Spring Boot SMTP de Vera funciona a la perfección.");

            mailSender.send(mensaje);
            System.out.println("✅ Email de prueba enviado exitosamente a: " + destinatario);
            
        } catch (Exception e) {
            System.err.println("❌ Error al enviar el email: " + e.getMessage());
        }
    }

    @Async
    public void enviarEmailAlertaRiesgoAlto(String destinatario, String nombreProtegido, String detalleAlerta) {
        try {
            MimeMessage mensaje = mailSender.createMimeMessage();
            
            MimeMessageHelper helper = new MimeMessageHelper(mensaje, true, "UTF-8");

            helper.setFrom(remitente);
            helper.setTo(destinatario);
            helper.setSubject("🚨 VERA: Acción Requerida - Riesgo Alto");

            String contenidoHtml = 
                "<div style='font-family: Arial, sans-serif; background-color: #0f172a; color: #f8fafc; padding: 40px 20px; text-align: center;'>" +
                    "<div style='max-width: 600px; margin: 0 auto; background-color: #1e293b; padding: 30px; border-radius: 8px; border-top: 4px solid #ef4444;'>" +
                        "<h1 style='color: #ef4444; margin-top: 0;'>🚨 Alerta de Seguridad</h1>" +
                        "<p style='font-size: 16px; color: #cbd5e1; text-align: left;'>Hola,</p>" +
                        "<p style='font-size: 16px; color: #cbd5e1; text-align: left;'>" +
                            "El sistema <strong>VERA</strong> ha detectado una nueva actividad de riesgo <strong>ALTO</strong> relacionada con tu protegido/a: <span style='color: #fff; font-weight: bold;'>" + nombreProtegido + "</span>." +
                        "</p>" +
                        "<div style='background-color: #334155; padding: 15px; border-radius: 6px; margin: 20px 0; text-align: left;'>" +
                            "<p style='margin: 0; color: #f8fafc; font-style: italic;'>\"" + detalleAlerta + "\"</p>" +
                        "</div>" +
                        "<p style='font-size: 14px; color: #94a3b8; text-align: left;'>" +
                            "Por favor, ingresa a la plataforma lo antes posible para revisar el historial completo y tomar las medidas necesarias." +
                        "</p>" +
                        "<a href='http://localhost:5173' style='display: inline-block; margin-top: 20px; padding: 12px 24px; background-color: #ef4444; color: #ffffff; text-decoration: none; font-weight: bold; border-radius: 6px;'>Ir al Dashboard</a>" +
                    "</div>" +
                    "<p style='font-size: 12px; color: #64748b; margin-top: 20px;'>Este es un mensaje automático de Vera. Por favor, no respondas a este correo.</p>" +
                "</div>";

            
            helper.setText(contenidoHtml, true);

            mailSender.send(mensaje);
            System.out.println("🚨 Email HTML de alerta crítica enviado exitosamente a: " + destinatario);
            
        } catch (Exception e) {
            System.err.println("❌ Error al enviar el email HTML de alerta: " + e.getMessage());
        }

        
    }

    @Async
    public void enviarEmailResumenSemanal(String destinatario, String nombreProtector, String nombreProtegido, 
                                          long alertasAltas, long alertasMedias, List<AlertsEntity> ultimasAlertas) {
        try {
            MimeMessage mensaje = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mensaje, true, "UTF-8");

            helper.setFrom(remitente);
            helper.setTo(destinatario);
            helper.setSubject("📊 VERA: Tu resumen semanal de actividad");

            // Formateador para que la fecha se vea bonita (ej: 12 Jun, 14:30)
            java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("dd MMM, HH:mm");

            // Construimos la lista de alertas destacadas en HTML
            StringBuilder alertasHtml = new StringBuilder();
            if (ultimasAlertas.isEmpty()) {
                alertasHtml.append("<p style='color: #10b981; text-align: left;'>✅ ¡Excelente semana! No se registraron alertas recientes.</p>");
            } else {
                for (AlertsEntity alerta : ultimasAlertas) {
                    String colorBorde = "ALTO".equalsIgnoreCase(alerta.getRiskLevel().toString()) ? "#ef4444" : "#eab308";
                    String estado = alerta.isResolved() ? "✅ Resuelta" : "⏳ Pendiente";
                    String fechaStr = alerta.getCreatedAt() != null ? alerta.getCreatedAt().format(formatter) : "Reciente";
                    String titulo = alerta.getTitle() != null ? alerta.getTitle() : "Alerta de seguridad";

                    alertasHtml.append("<div style='background-color: #334155; padding: 15px; border-radius: 6px; margin-bottom: 10px; text-align: left; border-left: 4px solid ")
                               .append(colorBorde).append(";'>")
                               .append("<p style='margin: 0; color: #f8fafc; font-weight: bold;'>").append(titulo).append("</p>")
                               .append("<p style='margin: 5px 0 0; color: #94a3b8; font-size: 12px;'>📅 ").append(fechaStr)
                               .append(" | Estado: <strong>").append(estado).append("</strong></p>")
                               .append("</div>");
                }
            }

            // Armamos el HTML final
            String contenidoHtml = 
                "<div style='font-family: Arial, sans-serif; background-color: #0f172a; color: #f8fafc; padding: 40px 20px; text-align: center;'>" +
                    "<div style='max-width: 600px; margin: 0 auto; background-color: #1e293b; padding: 30px; border-radius: 8px; border-top: 4px solid #3b82f6;'>" +
                        "<h1 style='color: #3b82f6; margin-top: 0;'>Resumen Semanal</h1>" +
                        "<p style='font-size: 16px; color: #cbd5e1; text-align: left;'>Hola " + nombreProtector + ",</p>" +
                        "<p style='font-size: 16px; color: #cbd5e1; text-align: left;'>" +
                            "Aquí tienes el resumen de actividad de los últimos 7 días para tu protegido/a: <strong>" + nombreProtegido + "</strong>." +
                        "</p>" +
                        "<div style='display: flex; justify-content: space-around; margin: 30px 0;'>" +
                            "<div style='background-color: #334155; padding: 20px; border-radius: 8px; width: 40%;'>" +
                                "<h2 style='margin: 0; color: #ef4444; font-size: 28px;'>" + alertasAltas + "</h2>" +
                                "<p style='margin: 5px 0 0; color: #94a3b8; font-size: 14px;'>Riesgos Altos</p>" +
                            "</div>" +
                            "<div style='background-color: #334155; padding: 20px; border-radius: 8px; width: 40%;'>" +
                                "<h2 style='margin: 0; color: #eab308; font-size: 28px;'>" + alertasMedias + "</h2>" +
                                "<p style='margin: 5px 0 0; color: #94a3b8; font-size: 14px;'>Riesgos Medios</p>" +
                            "</div>" +
                        "</div>" +
                        "<h3 style='color: #cbd5e1; margin-top: 30px; border-bottom: 1px solid #334155; padding-bottom: 10px; text-align: left;'>Actividad Reciente Destacada</h3>" +
                        alertasHtml.toString() + 
                        "<a href='http://localhost:5173' style='display: inline-block; margin-top: 25px; padding: 12px 24px; background-color: #3b82f6; color: #ffffff; text-decoration: none; font-weight: bold; border-radius: 6px;'>Ver historial completo</a>" +
                    "</div>" +
                    "<p style='font-size: 12px; color: #64748b; margin-top: 20px;'>Recibes este correo porque tienes activados los reportes semanales en Vera.</p>" +
                "</div>";

            helper.setText(contenidoHtml, true);
            mailSender.send(mensaje);
            System.out.println("📊 Resumen semanal detallado enviado a: " + destinatario);
            
        } catch (Exception e) {
            System.err.println("❌ Error al enviar el resumen semanal: " + e.getMessage());
        }
    }
}