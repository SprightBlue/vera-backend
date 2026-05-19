package com.unlam.verabackend.infrastructure.adapters.inbound.web;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class DashboardController {

    @GetMapping("/dashboard")
    public Map<String, Object> getDashboardData() {

        Map<String, Object> data = new HashMap<>();

        data.put("alerts", 5);
        data.put("analyses", 22);
        data.put("highRisk", 2);

        return data;
    }
}