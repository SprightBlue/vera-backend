package com.unlam.verabackend.application.usecases;

import com.unlam.verabackend.ports.inbound.ObtenerSaludoUseCase;
import org.springframework.stereotype.Service;

@Service
public class ObtenerSaludoUseCaseImpl implements ObtenerSaludoUseCase {

    @Override
    public String ejecutar() {
        return "¡Hola Mundo! El Back y el Front están conectados exitosamente 🚀";
    }
}