package com.unlam.verabackend.infrastructure.provider;

import com.unlam.verabackend.domain.port.out.EmailProvider;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class GmailEmailAdapter implements EmailProvider {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    @Override
    public void sendPasswordResetEmail(String to, String token) {
        log.info("Iniciando proceso de envío de correo de recuperación de contraseña para: {}", to);

        try {
            String resetLink = frontendUrl + "/reset-password?token=" + token;
            log.debug("Enlace de recuperación generado para {}: {}", to, resetLink);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject("Recuperación de contraseña - VERA");

            String html = getPasswordResetEmailTemplate(resetLink);

            helper.setText(html, true);
            mailSender.send(message);

            log.info("Correo de recuperación de contraseña enviado con éxito a: {}", to);

        } catch (Exception e) {
            log.error("Fallo crítico al enviar correo de recuperación a {}. Razón: {}", to, e.getMessage());
            throw new RuntimeException("No se pudo enviar el correo de recuperación de contraseña en este momento.", e);
        }
    }

    @Override
    public void sendVerificationEmail(String to, String token) {
        log.info("Iniciando proceso de envío de correo de verificación para: {}", to);

        try {
            String verificationLink = frontendUrl + "/verify-email?token=" + token;
            log.debug("Enlace de verificación generado para {}: {}", to, verificationLink);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject("Activá tu cuenta en VERA - Protección Digital");

            String html = getVerificationEmailTemplate(verificationLink);

            helper.setText(html, true);
            mailSender.send(message);

            log.info("Correo de verificación enviado con éxito a: {}", to);

        } catch (Exception e) {
            log.error("Fallo crítico al enviar correo de verificación a {}. Razón: {}", to, e.getMessage());
            throw new RuntimeException("No se pudo procesar el envío del correo de verificación en este momento.", e);
        }
    }

    private String getPasswordResetEmailTemplate(String resetLink) {
        return """
            <!DOCTYPE html>
            <html>
            <body style="margin:0; padding:0; background-color:#05070D; font-family:Arial,sans-serif;">
            <table width="100%%" cellpadding="0" cellspacing="0">
                <tr>
                    <td align="center" style="padding:40px 20px;">
                        <table width="600" cellpadding="0" cellspacing="0" style="background:#0B0D17; border-radius:20px; overflow:hidden; border:1px solid #1A1D29;">
                            <tr>
                                <td align="center" style="padding:50px 40px 20px 40px;">
                                    <img src="https://vera-app.vercel.app/Isologo_Vera.png" alt="VERA" width="180" />
                                </td>
                            </tr>
                            <tr>
                                <td align="center" style="padding:10px 40px;">
                                    <h1 style="color:white; margin:0; font-size:34px;">Recuperación de contraseña</h1>
                                </td>
                            </tr>
                            <tr>
                                <td align="center" style="padding:20px 50px;">
                                    <p style="color:#B7BDC9; font-size:16px; line-height:28px; margin:0;">
                                        Recibimos una solicitud para restablecer la contraseña de tu cuenta.
                                    </p>
                                </td>
                            </tr>
                            <tr>
                                <td align="center" style="padding:20px;">
                                    <a href="%s" style="background:#0D6EFD; color:white; text-decoration:none; padding:16px 32px; border-radius:12px; display:inline-block; font-size:16px; font-weight:bold;">
                                        Restablecer contraseña
                                    </a>
                                </td>
                            </tr>
                            <tr>
                                <td align="center" style="padding:10px 50px 30px 50px;">
                                    <p style="color:#8A91A1; font-size:14px; line-height:24px;">
                                        Si el botón no funciona, copiá y pegá este enlace en tu navegador:
                                    </p>
                                    <a href="%s" style="color:#4D9DFF; word-break:break-all;">%s</a>
                                </td>
                            </tr>
                            <tr>
                                <td align="center" style="padding:20px 50px; border-top:1px solid #1A1D29;">
                                    <p style="color:#B7BDC9; font-size:14px; margin:0;">Este enlace expirará en 1 hora.</p>
                                    <p style="color:#8A91A1; font-size:13px; margin-top:20px;">Si no realizaste esta solicitud, podés ignorar este correo.</p>
                                    <p style="color:white; margin-top:30px; font-weight:bold;">Equipo VERA</p>
                                </td>
                            </tr>
                        </table>
                    </td>
                </tr>
            </table>
            </body>
            </html>
            """.formatted(resetLink, resetLink, resetLink);
    }

    private String getVerificationEmailTemplate(String verificationLink) {
        return """
            <!DOCTYPE html>
            <html>
            <body style="margin:0; padding:0; background-color:#05070D; font-family:Arial,sans-serif;">
                <table width="100%%" cellpadding="0" cellspacing="0">
                    <tr>
                        <td align="center" style="padding:40px 20px;">
                            <table width="600" cellpadding="0" cellspacing="0" style="background:#0B0D17; border-radius:20px; overflow:hidden; border:1px solid #1A1D29;">
                                <tr>
                                    <td align="center" style="padding:50px 40px 20px 40px;">
                                        <img src="https://vera-app.vercel.app/Isologo_Vera.png" alt="VERA" width="180" />
                                    </td>
                                </tr>
                                <tr>
                                    <td align="center" style="padding:10px 40px;">
                                        <h1 style="color:white; margin:0; font-size:30px;">¡Te damos la bienvenida a VERA!</h1>
                                    </td>
                                </tr>
                                <tr>
                                    <td align="center" style="padding:20px 50px;">
                                        <p style="color:#B7BDC9; font-size:16px; line-height:28px; margin:0;">
                                            Estás a un solo paso de activar tu cuenta y empezar a proteger a las personas que más querés contra estafas digitales.
                                        </p>
                                    </td>
                                </tr>
                                <tr>
                                    <td align="center" style="padding:20px;">
                                        <a href="%s" style="background:#0D6EFD; color:white; text-decoration:none; padding:16px 32px; border-radius:12px; display:inline-block; font-size:16px; font-weight:bold;">
                                            Verificar mi Correo
                                        </a>
                                    </td>
                                </tr>
                                <tr>
                                    <td align="center" style="padding:10px 50px 30px 50px; border-top:1px solid #1A1D29;">
                                        <p style="color:#8A91A1; font-size:14px; line-height:24px; margin:0;">
                                            Si el botón no funciona, copiá y pegá este enlace en tu navegador:
                                        </p>
                                        <a href="%s" style="color:#4D9DFF; word-break:break-all;">%s</a>
                                        <p style="color:#8A91A1; font-size:13px; margin-top:20px;">
                                            Este enlace expirará en 24 horas.
                                        </p>
                                        <p style="color:white; margin-top:30px; font-weight:bold;">Equipo VERA</p>
                                    </td>
                                </tr>
                            </table>
                        </td>
                    </tr>
                </table>
            </body>
            </html>
            """.formatted(verificationLink, verificationLink, verificationLink);
    }
}