package com.unlam.verabackend.presentation.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class DashboardController {

    @GetMapping("/dashboard")
    public Map<String, Object> getDashboardData() {

        Map<String, Object> data = new HashMap<>();

        data.put("alerts", 10);
        data.put("analyses", 2);
        data.put("highRisk", 3);

        return data;
    }
}
