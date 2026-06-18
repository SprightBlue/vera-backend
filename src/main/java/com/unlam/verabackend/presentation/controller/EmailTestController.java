package com.unlam.verabackend.presentation.controller;

import com.unlam.verabackend.infrastructure.provider.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class EmailTestController {

    @Autowired
    private EmailService emailService;

    @GetMapping("/api/test-email")
    public String probarEmail(@RequestParam String correo) {
        try {
            // Ahora llamamos al método nuevo con el diseño HTML y le pasamos datos de prueba
            emailService.enviarEmailAlertaRiesgoAlto(
                correo, 
                "Juan Pérez (Prueba)", 
                "Se detectó vocabulario agresivo y presión para compartir información personal en la red social Instagram."
            );
            return "📬 Intento de correo HTML programado para: " + correo;
        } catch (Exception e) {
            return "❌ Error al intentar gatillar el email: " + e.getMessage();
        }
    }
}
