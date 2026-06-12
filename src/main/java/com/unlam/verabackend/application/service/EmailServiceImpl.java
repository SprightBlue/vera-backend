package com.unlam.verabackend.application.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
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
            String token
    ) {

        String resetLink =
                "http://localhost:5173/reset-password?token="
                        + token;

        SimpleMailMessage message =
                new SimpleMailMessage();

        message.setFrom(fromEmail);

        message.setTo(to);

        message.setSubject(
                "Recuperación de contraseña - VERA"
        );

        message.setText(
                """
                Hola,

                Recibimos una solicitud para restablecer tu contraseña.

                Utiliza el siguiente enlace:

                %s

                Si no realizaste esta solicitud puedes ignorar este correo.

                Equipo VERA
                """.formatted(resetLink)
        );

        mailSender.send(message);
    }
}