package com.unlam.verabackend.application.service;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private MimeMessage mimeMessage;

    @InjectMocks
    private EmailServiceImpl emailService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(emailService, "fromEmail", "tomas@pruebas.com");
    }

    @Test
    void deberiaEnviarEmailDeRecuperacionDeContrasenaConExito() {
        String to = "tomas@gmail.com";
        String token = "token123";
        
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        emailService.sendPasswordResetEmail(to, token);

        verify(mailSender, times(1)).send(mimeMessage);
    }

    @Test
    void deberiaLanzarExcepcionSiFallaElEnvioDeRecuperacion() {
        String to = "tomas@gmail.com";
        String token = "token123";
        
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        doThrow(new RuntimeException("Error SMTP")).when(mailSender).send(mimeMessage);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            emailService.sendPasswordResetEmail(to, token);
        });
        
        assertTrue(exception.getMessage().contains("Error enviando email"));
    }

    @Test
    void deberiaEnviarEmailDeVerificacionConExito() {
        String to = "tomas@gmail.com";
        String token = "123456789";
        
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        emailService.sendVerificationEmail(to, token);

        verify(mailSender, times(1)).send(mimeMessage);
    }

    @Test
    void deberiaLanzarExcepcionSiFallaElEnvioDeVerificacion() {
        String to = "tomas@gmail.com";
        String token = "123456789";
        
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        doThrow(new RuntimeException("Timeout")).when(mailSender).send(mimeMessage);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            emailService.sendVerificationEmail(to, token);
        });

        assertTrue(exception.getMessage().contains("Error enviando el email de verificación"));
    }
}