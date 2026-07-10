package com.unlam.verabackend.infrastructure.provider;

import com.unlam.verabackend.domain.port.out.EmailService;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

        private final JavaMailSender mailSender;

        @Value("${spring.mail.username}")
        private String fromEmail;

        @Override
        public void sendPasswordResetEmail(
                        String to,
                        String token) {

                try {

                        String resetLink = "http://localhost:5173/reset-password?token="
                                        + token;

                        MimeMessage message = mailSender.createMimeMessage();

                        MimeMessageHelper helper = new MimeMessageHelper(
                                        message,
                                        true,
                                        "UTF-8");

                        helper.setFrom(fromEmail);
                        helper.setTo(to);

                        helper.setSubject(
                                        "Recuperación de contraseña - VERA");

                        String html = """
                                                        <!DOCTYPE html>
                                                        <html>
                                                        <body style="
                                                            margin:0;
                                                            padding:0;
                                                            background-color:#05070D;
                                                            font-family:Arial,sans-serif;
                                                        ">

                                                        <table width="100%%" cellpadding="0" cellspacing="0">
                                                            <tr>
                                                                <td align="center" style="padding:40px 20px;">

                                                                    <table
                                                                        width="600"
                                                                        cellpadding="0"
                                                                        cellspacing="0"
                                                                        style="
                                                                            background:#0B0D17;
                                                                            border-radius:20px;
                                                                            overflow:hidden;
                                                                            border:1px solid #1A1D29;
                                                                        "
                                                                    >

                                                                        <tr>
                                                                            <td
                                                                                align="center"
                                                                                style="padding:50px 40px 20px 40px;"
                                                                            >

                                                                                <img
                                         src="https://vera-app.vercel.app/Isologo_Vera.png"
                                         alt="VERA"
                                         width="180"
                                        />

                                                                            </td>
                                                                        </tr>

                                                                        <tr>
                                                                            <td
                                                                                align="center"
                                                                                style="padding:10px 40px;"
                                                                            >

                                                                                <h1 style="
                                                                                    color:white;
                                                                                    margin:0;
                                                                                    font-size:34px;
                                                                                ">
                                                                                    Recuperación de contraseña
                                                                                </h1>

                                                                            </td>
                                                                        </tr>

                                                                        <tr>
                                                                            <td
                                                                                align="center"
                                                                                style="padding:20px 50px;"
                                                                            >

                                                                                <p style="
                                                                                    color:#B7BDC9;
                                                                                    font-size:16px;
                                                                                    line-height:28px;
                                                                                    margin:0;
                                                                                ">
                                                                                    Recibimos una solicitud para
                                                                                    restablecer la contraseña de tu cuenta.
                                                                                </p>

                                                                            </td>
                                                                        </tr>

                                                                        <tr>
                                                                            <td
                                                                                align="center"
                                                                                style="padding:20px;"
                                                                            >

                                                                                <a
                                                                                    href="%s"
                                                                                    style="
                                                                                        background:#0D6EFD;
                                                                                        color:white;
                                                                                        text-decoration:none;
                                                                                        padding:16px 32px;
                                                                                        border-radius:12px;
                                                                                        display:inline-block;
                                                                                        font-size:16px;
                                                                                        font-weight:bold;
                                                                                    "
                                                                                >
                                                                                    Restablecer contraseña
                                                                                </a>

                                                                            </td>
                                                                        </tr>

                                                                        <tr>
                                                                            <td
                                                                                align="center"
                                                                                style="
                                                                                    padding:10px 50px 30px 50px;
                                                                                "
                                                                            >

                                                                                <p style="
                                                                                    color:#8A91A1;
                                                                                    font-size:14px;
                                                                                    line-height:24px;
                                                                                ">
                                                                                    Si el botón no funciona,
                                                                                    copiá y pegá este enlace
                                                                                    en tu navegador:
                                                                                </p>

                                                                                <a
                                                                                    href="%s"
                                                                                    style="
                                                                                        color:#4D9DFF;
                                                                                        word-break:break-all;
                                                                                    "
                                                                                >
                                                                                    %s
                                                                                </a>

                                                                            </td>
                                                                        </tr>

                                                                        <tr>
                                                                            <td
                                                                                align="center"
                                                                                style="
                                                                                    padding:20px 50px;
                                                                                    border-top:1px solid #1A1D29;
                                                                                "
                                                                            >

                                                                                <p style="
                                                                                    color:#B7BDC9;
                                                                                    font-size:14px;
                                                                                    margin:0;
                                                                                ">
                                                                                    Este enlace expirará en 1 hora.
                                                                                </p>

                                                                                <p style="
                                                                                    color:#8A91A1;
                                                                                    font-size:13px;
                                                                                    margin-top:20px;
                                                                                ">
                                                                                    Si no realizaste esta solicitud,
                                                                                    podés ignorar este correo.
                                                                                </p>

                                                                                <p style="
                                                                                    color:white;
                                                                                    margin-top:30px;
                                                                                    font-weight:bold;
                                                                                ">
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
                                                        """
                                        .formatted(
                                                        resetLink,
                                                        resetLink,
                                                        resetLink);

                        helper.setText(html, true);

                        mailSender.send(message);

                } catch (Exception e) {

                        throw new RuntimeException(
                                        "Error enviando email",
                                        e);
                }
        }



        @Override
    public void sendVerificationEmail(String to, String token) {
        try {
            String verificationLink = "http://localhost:5173/verify-email?token=" + token;

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject("Activá tu cuenta en VERA - Protección Digital");

            String html = """
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

            helper.setText(html, true);
            mailSender.send(message);
            System.out.println("📩 Email de verificación enviado con éxito a: " + to);

        } catch (Exception e) {
            throw new RuntimeException("Error enviando el email de verificación", e);
        }
    }

    }