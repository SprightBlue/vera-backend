package com.unlam.verabackend.application.usecase;

import com.unlam.verabackend.domain.exception.ResourceNotFoundException;
import com.unlam.verabackend.domain.model.UserLocation;
import com.unlam.verabackend.domain.port.in.GetUserLocationUseCase;
import com.unlam.verabackend.domain.port.out.UserLocationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class GetUserLocationUseCaseImpl implements GetUserLocationUseCase {

    private final UserLocationRepository locationRepository;

    @Override
    @Transactional(readOnly = true)
    public UserLocation execute(Long trustContactId, String carerEmail) {
        log.info("UseCase: Solicitando última ubicación para la relación ID [{}] por el cuidador [{}]", trustContactId, carerEmail);

        UserLocation userLocation = locationRepository.findByTrustContactId(trustContactId)
                .orElseThrow(() -> {
                    log.warn("UseCase: No se encontraron registros de ubicación para el vínculo ID [{}]", trustContactId);
                    return new ResourceNotFoundException("No hay registros de ubicación para esta relación.");
                });

        if (!userLocation.getTrustContact().getCarer().getEmail().equals(carerEmail)) {
            log.error("ALERTA DE SEGURIDAD: El usuario [{}] intentó acceder de manera ilegítima a la ubicación de la relación ID [{}]", carerEmail, trustContactId);
            throw new SecurityException("Acceso denegado: No eres el cuidador asignado.");
        }

        log.info("UseCase: Ubicación obtenida exitosamente para el vínculo ID [{}]", trustContactId);
        return userLocation;
    }
}