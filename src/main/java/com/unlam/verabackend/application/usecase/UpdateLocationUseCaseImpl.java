package com.unlam.verabackend.application.usecase;

import com.unlam.verabackend.domain.port.in.UpdateLocationUseCase;
import com.unlam.verabackend.domain.model.UserLocation;
import com.unlam.verabackend.domain.port.out.GeoLocationProvider;
import com.unlam.verabackend.domain.port.out.UserLocationRepository;
import com.unlam.verabackend.infrastructure.entity.TrustContact;
import com.unlam.verabackend.infrastructure.repository.TrustContactRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.NoSuchElementException;

@Slf4j
@Service
@RequiredArgsConstructor
public class UpdateLocationUseCaseImpl implements UpdateLocationUseCase {

    private final UserLocationRepository repository;
    private final GeoLocationProvider geoService;
    private final TrustContactRepository trustContactRepository;

    @Override
    @Transactional
    public UserLocation execute(String email, BigDecimal lat, BigDecimal lon) {
        log.info("Actualizando ubicación para usuario protegido: {}", email);

        TrustContact contact = trustContactRepository.findByProtectedUser_Email(email)
                .orElseThrow(() -> {
                    log.error("Relación no encontrada para: {}", email);
                    return new NoSuchElementException("Relación no encontrada");
                });

        UserLocation location = repository.findByTrustContactId(contact.getId())
                .orElse(UserLocation.builder().trustContact(contact).build());

        location.setLatitude(lat);
        location.setLongitude(lon);
        location.setLocationText(geoService.getAddressFromCoords(lat, lon));
        location.setConnected(true);

        return repository.save(location, contact.getId());
    }
}