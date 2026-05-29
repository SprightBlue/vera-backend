package com.unlam.verabackend.presentation.controller;

import com.unlam.verabackend.application.service.AlertService;
import com.unlam.verabackend.infrastructure.entity.AlertEntity;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.CrossOrigin;
import java.util.List;

@RestController
@RequestMapping("/api/alerts")
@CrossOrigin(origins = "*")
public class AlertController {

    private final AlertService alertService;

    public AlertController(AlertService alertService) {
        this.alertService = alertService;
    }

    @GetMapping
    public ResponseEntity<List<AlertEntity>> getAlerts() {
        List<AlertEntity> alerts = alertService.getAllAlerts();
        return ResponseEntity.ok(alerts);
    }
}