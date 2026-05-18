package com.unlam.verabackend.infrastructure.adapters.inbound.web;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RootController {

    @GetMapping("/")
    public String index() {
        return "<!DOCTYPE html>" +
                "<html lang='es'>" +
                "<head>" +
                "    <meta charset='UTF-8'>" +
                "    <meta name='viewport' content='width=device-width, initial-scale=1.0'>" +
                "    <title>Vera App - Backend Status</title>" +
                "    <style>" +
                "        body { font-family: system-ui, -apple-system, sans-serif; background-color: #020617; color: #f8fafc; display: flex; justify-content: center; align-items: center; height: 100vh; margin: 0; }" +
                "        .card { background-color: #0f172a; padding: 2.5rem; rounded-radius: 1rem; border: 1px solid #1e293b; border-radius: 16px; text-align: center; box-shadow: 0 20px 25px -5px rgb(0 0 0 / 0.5); max-width: 400px; }" +
                "        .badge { background-color: rgba(16, 185, 129, 0.1); color: #10b981; border: 1px solid rgba(16, 185, 129, 0.2); padding: 0.25rem 0.75rem; border-radius: 9999px; font-size: 0.875rem; font-weight: 600; display: inline-block; mb-4: 1rem; margin-bottom: 1rem; }" +
                "        h1 { margin: 0 0 0.5rem 0; font-size: 1.75rem; font-weight: 700; }" +
                "        p { color: #94a3b8; font-size: 0.95rem; line-height: 1.5; margin: 0; }" +
                "        .footer { margin-top: 2rem; font-size: 0.75rem; color: #64748b; font-family: monospace; }" +
                "    </style>" +
                "</head>" +
                "<body>" +
                "    <div class='card'>" +
                "        <span class='badge'>● ONLINE</span>" +
                "        <h1>Vera API</h1>" +
                "        <p>El backend de Spring Boot se encuentra activo y respondiendo correctamente en la nube.</p>" +
                "        <div class='footer'>v1.0.0 • Connected to Neon DB</div>" +
                "    </div>" +
                "</body>" +
                "</html>";
    }
}