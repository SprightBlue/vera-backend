package com.unlam.verabackend.infrastructure.adapters.inbound.web;

import com.unlam.verabackend.domain.entities.Saludo;
import com.unlam.verabackend.infrastructure.adapters.outbound.database.SaludoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class SaludoController {

    @Autowired
    private SaludoRepository saludoRepository;

    @GetMapping("/hola")
    public Map<String, String> obtenerHolaMundo() {
        Map<String, String> respuesta = new HashMap<>();

        Saludo saludo = saludoRepository.findAll().stream()
                .findFirst()
                .orElseGet(() -> saludoRepository.save(new Saludo("¡Hola Mundo real desde la Base de Datos de Neon! 🚀")));

        respuesta.put("mensaje", saludo.getMensaje());
        return respuesta;
    }
}