package com.unlam.verabackend.application.usecase;

import com.unlam.verabackend.domain.exception.ResourceNotFoundException;
import com.unlam.verabackend.domain.port.in.CheckProtectedUserStatusUseCase;
import com.unlam.verabackend.infrastructure.repository.TrustContactRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CheckProtectedUserStatusUseCaseImpl implements CheckProtectedUserStatusUseCase {

    private final TrustContactRepository trustContactRepository;

    @Override
    @Transactional(readOnly = true)
    public boolean execute(String email) {
        log.info("UseCase: Verificando estado de protección para el usuario con email: [{}]", email);

        boolean isProtected = trustContactRepository.findByProtectedUser_Email(email).isPresent();

        if (!isProtected) {
            log.warn("UseCase: El usuario [{}] no posee ninguna relación de contacto de confianza activa.", email);
            throw new ResourceNotFoundException("El usuario con email " + email + " no está registrado como protegido.");
        }

        log.info("UseCase: Confirmado. El usuario [{}] es un protegido activo en el sistema.", email);
        return true;
    }
}