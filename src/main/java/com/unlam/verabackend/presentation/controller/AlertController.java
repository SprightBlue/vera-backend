package com.unlam.verabackend.presentation.controller;

import com.unlam.verabackend.domain.model.AlertResponse;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class AlertController {

    @GetMapping("/alerts")
    public List<AlertResponse> getAlerts() {

        return List.of(
                new AlertResponse(
                        1L,
                        "Posible phishing detectado",
                        "ALTO"
                ),
                new AlertResponse(
                        2L,
                        "Mensaje sospechoso en WhatsApp",
                        "MEDIO"
                ),
                new AlertResponse(
                        3L,
                        "Enlace potencialmente peligroso",
                        "ALTO"
                )
        );
    }
}