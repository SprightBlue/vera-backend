package com.unlam.verabackend.infrastructure.provider;

import com.google.api.client.googleapis.auth.oauth2.GoogleRefreshTokenRequest;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.unlam.verabackend.domain.port.out.EmailProvider;
import jakarta.mail.Session;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import java.io.ByteArrayOutputStream;
import java.util.Map;
import java.util.Properties;

@Slf4j
@Service
public class GmailEmailAdapter implements EmailProvider {

    private final String clientId;
    private final String clientSecret;
    private final String refreshToken;
    private final String frontendUrl;
    private final RestTemplate restTemplate;

    public GmailEmailAdapter(
            @Value("${google.client.id}") String clientId,
            @Value("${google.client.secret}") String clientSecret,
            @Value("${google.refresh.token}") String refreshToken,
            @Value("${app.frontend.url}") String frontendUrl) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.refreshToken = refreshToken;
        this.frontendUrl = frontendUrl;
        this.restTemplate = new RestTemplate();
    }

    @Override
    public void sendPasswordResetEmail(String to, String token) throws Exception {
        log.info("Iniciando proceso de envío de correo de recuperación (Gmail API HTTP) para: {}", to);

        String resetLink = frontendUrl + "/reset-password?token=" + token;
        String htmlContent = getPasswordResetEmailTemplate(resetLink);

        sendEmailViaHttp(to, "Recuperación de contraseña - VERA", htmlContent);

        log.info("Correo de recuperación de contraseña enviado con éxito a: {}", to);
    }

    @Override
    public void sendVerificationEmail(String to, String token) throws Exception {
        log.info("Iniciando proceso de envío de correo de verificación (Gmail API HTTP) para: {}", to);

        String verificationLink = frontendUrl + "/verify-email?token=" + token;
        String htmlContent = getVerificationEmailTemplate(verificationLink);

        sendEmailViaHttp(to, "Activá tu cuenta en VERA - Protección Digital", htmlContent);

        log.info("Correo de verificación enviado con éxito a: {}", to);
    }

    private void sendEmailViaHttp(String to, String subject, String htmlContent) throws Exception {
        GoogleTokenResponse tokenResponse = new GoogleRefreshTokenRequest(
                new NetHttpTransport(),
                new GsonFactory(),
                refreshToken,
                clientId,
                clientSecret
        ).execute();

        String accessToken = tokenResponse.getAccessToken();

        Properties props = new Properties();
        Session session = Session.getDefaultInstance(props, null);
        MimeMessage email = new MimeMessage(session);

        email.setFrom(new InternetAddress("me"));
        email.addRecipient(MimeMessage.RecipientType.TO, new InternetAddress(to));
        email.setSubject(subject);
        email.setContent(htmlContent, "text/html; charset=utf-8");

        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        email.writeTo(buffer);
        byte[] bytes = buffer.toByteArray();
        String encodedEmail = Base64.encodeBase64URLSafeString(bytes);

        String url = "https://gmail.googleapis.com/gmail/v1/users/me/messages/send";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, String> requestBody = Map.of("raw", encodedEmail);
        HttpEntity<Map<String, String>> request = new HttpEntity<>(requestBody, headers);

        restTemplate.postForEntity(url, request, String.class);
    }

    private String getPasswordResetEmailTemplate(String resetLink) {
        return """
            <!DOCTYPE html>
            <html lang="es">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Recuperación de contraseña - VERA</title>
            </head>
            <body style="margin:0; padding:0; background-color:#03050a; font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',Roboto,Helvetica,Arial,sans-serif; -webkit-font-smoothing:antialiased;">
            <table width="100%%" cellpadding="0" cellspacing="0" style="background-color:#03050a; min-height: 100vh;">
                <tr>
                    <td align="center" style="padding:40px 20px;">
                        <table width="100%%" style="max-width:560px; background:#070a13; border-radius:16px; overflow:hidden; border:1px solid #161f38; box-shadow:0 20px 40px rgba(0,0,0,0.5);" cellpadding="0" cellspacing="0">
                            <tr>
                                <td align="center" style="padding:48px 40px 24px 40px;">
                                    <img src="https://vera-frontend-gamma.vercel.app/Isologo_Vera.png" alt="VERA" width="160" style="display:block; border:0;" />
                                </td>
                            </tr>
                            <tr>
                                <td align="center" style="padding:0 40px;">
                                    <div style="height:1px; width:80px; background-color:#3b82f6; opacity:0.3;"></div>
                                </td>
                            </tr>
                            <tr>
                                <td align="center" style="padding:24px 40px 16px 40px;">
                                    <h1 style="color:#ffffff; margin:0; font-size:28px; font-weight:800; tracking: -0.025em; text-transform:uppercase; letter-spacing:1px;">
                                        Recuperación de contraseña
                                    </h1>
                                </td>
                            </tr>
                            <tr>
                                <td align="center" style="padding:0 50px 24px 50px;">
                                    <p style="color:#94a3b8; font-size:15px; line-height:1.625; margin:0; font-weight:400;">
                                        Recibimos una solicitud para restablecer la contraseña de tu cuenta de seguridad. Si no la solicitaste, puedes ignorar este correo con total tranquilidad.
                                    </p>
                                </td>
                            </tr>
                            <tr>
                                <td align="center" style="padding:16px 40px 32px 40px;">
                                    <table cellpadding="0" cellspacing="0" style="border-collapse:separate;">
                                        <tr>
                                            <td align="center" style="border-radius:8px; background-color:#2563eb; box-shadow: 0 10px 15px -3px rgba(37, 99, 235, 0.3);">
                                                <a href="%s" style="display:inline-block; padding:14px 28px; color:#ffffff; font-size:14px; font-weight:700; text-decoration:none; text-transform:uppercase; letter-spacing:1px; border-radius:8px;">
                                                    Restablecer contraseña
                                                </a>
                                            </td>
                                        </tr>
                                    </table>
                                </td>
                            </tr>
                            <tr>
                                <td align="center" style="padding:24px 40px; background-color:#05070e; border-top:1px solid #111827;">
                                    <p style="color:#64748b; font-size:13px; line-height:1.5; margin:0 0 12px 0;">
                                        Si el botón no funciona, copia y pega este enlace en tu navegador:
                                    </p>
                                    <div style="background-color:#0a0e1a; border:1px solid #1e293b; padding:12px; border-radius:8px; max-width:440px; word-break:break-all;">
                                        <a href="%s" style="color:#3b82f6; text-decoration:none; font-size:12px; font-family:monospace; word-break:break-all;">%s</a>
                                    </div>
                                </td>
                            </tr>
                            <tr>
                                <td align="center" style="padding:32px 40px; background-color:#04060b; border-top:1px solid #111827;">
                                    <p style="color:#475569; font-size:12px; margin:0 0 8px 0; font-weight:500;">
                                        Este enlace de seguridad expirará en un lapso de 1 hora.
                                    </p>
                                    <p style="color:#64748b; font-size:11px; margin:0 0 24px 0;">
                                        Por motivos de seguridad, nunca compartas este correo ni el enlace de restablecimiento.
                                    </p>
                                    <p style="color:#ffffff; font-size:12px; margin:0; font-weight:700; text-transform:uppercase; letter-spacing:1.5px;">
                                        Equipo VERA
                                    </p>
                                </td>
                            </tr>
                        </table>
                    </td>
                </tr>
            </table>
            </body>
            </html>
           \s""".formatted(resetLink, resetLink, resetLink);
    }

    private String getVerificationEmailTemplate(String verificationLink) {
        return """
            <!DOCTYPE html>
            <html lang="es">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Verificación de Cuenta - VERA</title>
            </head>
            <body style="margin:0; padding:0; background-color:#03050a; font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',Roboto,Helvetica,Arial,sans-serif; -webkit-font-smoothing:antialiased;">
            <table width="100%%" cellpadding="0" cellspacing="0" style="background-color:#03050a; min-height: 100vh;">
                <tr>
                    <td align="center" style="padding:40px 20px;">
                        <table width="100%%" style="max-width:560px; background:#070a13; border-radius:16px; overflow:hidden; border:1px solid #161f38; box-shadow:0 20px 40px rgba(0,0,0,0.5);" cellpadding="0" cellspacing="0">
                            <tr>
                                <td align="center" style="padding:48px 40px 24px 40px;">
                                    <img src="https://vera-frontend-gamma.vercel.app/Isologo_Vera.png" alt="VERA" width="160" style="display:block; border:0;" />
                                </td>
                            </tr>
                            <tr>
                                <td align="center" style="padding:0 40px;">
                                    <div style="height:1px; width:80px; background-color:#2563eb; opacity:0.3;"></div>
                                </td>
                            </tr>
                            <tr>
                                <td align="center" style="padding:24px 40px 16px 40px;">
                                    <h1 style="color:#ffffff; margin:0; font-size:26px; font-weight:800; tracking: -0.025em; text-transform:uppercase; letter-spacing:1px; line-height:1.2;">
                                        ¡Te damos la bienvenida!
                                    </h1>
                                </td>
                            </tr>
                            <tr>
                                <td align="center" style="padding:0 50px 24px 50px;">
                                    <p style="color:#94a3b8; font-size:15px; line-height:1.625; margin:0; font-weight:400;">
                                        Estás a un solo paso de activar tu cuenta y empezar a proteger a las personas que más quieres contra estafas digitales en tiempo real.
                                    </p>
                                </td>
                            </tr>
                            <tr>
                                <td align="center" style="padding:16px 40px 32px 40px;">
                                    <table cellpadding="0" cellspacing="0" style="border-collapse:separate;">
                                        <tr>
                                            <td align="center" style="border-radius:8px; background-color:#2563eb; box-shadow: 0 10px 15px -3px rgba(37, 99, 235, 0.3);">
                                                <a href="%s" style="display:inline-block; padding:14px 32px; color:#ffffff; font-size:14px; font-weight:700; text-decoration:none; text-transform:uppercase; letter-spacing:1px; border-radius:8px;">
                                                    Verificar mi Correo
                                                </a>
                                            </td>
                                        </tr>
                                    </table>
                                </td>
                            </tr>
                            <tr>
                                <td align="center" style="padding:24px 40px; background-color:#05070e; border-top:1px solid #111827;">
                                    <p style="color:#64748b; font-size:13px; line-height:1.5; margin:0 0 12px 0;">
                                        Si el botón no funciona, copia y pega este enlace en tu navegador:
                                    </p>
                                    <div style="background-color:#0a0e1a; border:1px solid #1e293b; padding:12px; border-radius:8px; max-width:440px; word-break:break-all;">
                                        <a href="%s" style="color:#3b82f6; text-decoration:none; font-size:12px; font-family:monospace; word-break:break-all;">%s</a>
                                    </div>
                                </td>
                            </tr>
                            <tr>
                                <td align="center" style="padding:32px 40px; background-color:#04060b; border-top:1px solid #111827;">
                                    <p style="color:#475569; font-size:12px; margin:0 0 8px 0; font-weight:500;">
                                        Este enlace de validación expirará en 24 horas.
                                    </p>
                                    <p style="color:#64748b; font-size:11px; margin:0 0 24px 0;">
                                        Si no has creado una cuenta en nuestra plataforma, puedes ignorar este mensaje.
                                    </p>
                                    <p style="color:#ffffff; font-size:12px; margin:0; font-weight:700; text-transform:uppercase; letter-spacing:1.5px;">
                                        Equipo VERA
                                    </p>
                                </td>
                            </tr>
                        </table>
                    </td>
                </tr>
            </table>
            </body>
            </html>
           \s""".formatted(verificationLink, verificationLink, verificationLink);
    }
}