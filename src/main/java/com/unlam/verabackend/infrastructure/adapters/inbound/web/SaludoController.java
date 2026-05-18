package com.unlam.verabackend.infrastructure.adapters.inbound.web;

import com.unlam.verabackend.ports.inbound.ObtenerSaludoUseCase;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
public class SaludoController {

    private final ObtenerSaludoUseCase obtenerSaludoUseCase;

    public SaludoController(ObtenerSaludoUseCase obtenerSaludoUseCase) {
        this.obtenerSaludoUseCase = obtenerSaludoUseCase;
    }

    @GetMapping("/hola")
    public Map<String, String> obtenerHolaMundo() {
        return Map.of("mensaje", obtenerSaludoUseCase.ejecutar());
    }
}