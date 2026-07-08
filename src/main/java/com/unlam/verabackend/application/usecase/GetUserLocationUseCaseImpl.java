package com.unlam.verabackend.application.usecase;

import com.unlam.verabackend.domain.exception.ResourceNotFoundException;
import com.unlam.verabackend.domain.model.UserLocation;
import com.unlam.verabackend.domain.port.in.GetUserLocationUseCase;
import com.unlam.verabackend.domain.port.out.UserLocationRepository;
import com.unlam.verabackend.infrastructure.entity.User;
import com.unlam.verabackend.domain.model.Role;
import com.unlam.verabackend.infrastructure.entity.TrustContact;
import com.unlam.verabackend.infrastructure.repository.UserRepository;
import com.unlam.verabackend.infrastructure.repository.TrustContactRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class GetUserLocationUseCaseImpl implements GetUserLocationUseCase {

    private final UserLocationRepository locationRepository;
    private final UserRepository userRepository;
    private final TrustContactRepository trustContactRepository;

    @Override
    @Transactional(readOnly = true)
    public UserLocation execute(Long trustContactId, String carerEmail) {
        log.info("UseCase: El CARER [{}] solicita inspeccionar el mapa de la relación intermedia ID [{}]", carerEmail, trustContactId);

        validateCarerRole(carerEmail);
        TrustContact contact = validateAndGetTrustContact(trustContactId, carerEmail);

        log.debug("UseCase: Vínculo verificado. Extrayendo coordenadas geográficas activas para el protegido [{}]", contact.getProtectedUser().getEmail());
        return locationRepository.findByProtectedUserEmail(contact.getProtectedUser().getEmail())
                .orElseThrow(() -> {
                    log.warn("UseCase: El vínculo ID [{}] existe pero el protegido aún no ha emitido coordenadas iniciales.", trustContactId);
                    return new ResourceNotFoundException("No hay registros geográficos disponibles para esta relación de confianza.");
                });
    }

    private void validateCarerRole(String email) {
        User requester = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario solicitante no registrado en el sistema."));

        if (!Role.CARER.equals(requester.getRole())) {
            log.error("Security Violation: Intento de lectura de mapa por parte de un usuario sin rol CARER: [{}]", email);
            throw new SecurityException("Acceso denegado: Solo los cuidadores autorizados pueden visualizar mapas en tiempo real.");
        }
    }

    private TrustContact validateAndGetTrustContact(Long id, String carerEmail) {
        TrustContact contact = trustContactRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("La relación de confianza especificada no existe."));

        if (!contact.getCarer().getEmail().equalsIgnoreCase(carerEmail)) {
            log.error("ALERTA DE SEGURIDAD: El cuidador [{}] intentó acceder ilegalmente al mapa del vínculo ID [{}]", carerEmail, id);
            throw new SecurityException("Acceso denegado: No eres el cuidador asignado a este protegido.");
        }
        return contact;
    }
}