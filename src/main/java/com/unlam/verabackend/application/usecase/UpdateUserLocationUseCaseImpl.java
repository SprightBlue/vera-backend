package com.unlam.verabackend.application.usecase;

import com.unlam.verabackend.domain.exception.ResourceNotFoundException;
import com.unlam.verabackend.domain.model.UserLocation;
import com.unlam.verabackend.domain.port.in.UpdateUserLocationUseCase;
import com.unlam.verabackend.domain.port.out.GeocodingProvider;
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
import java.math.BigDecimal;

@Slf4j
@Service
@RequiredArgsConstructor
public class UpdateUserLocationUseCaseImpl implements UpdateUserLocationUseCase {

    private final UserLocationRepository locationRepository;
    private final UserRepository userRepository;
    private final TrustContactRepository trustContactRepository;
    private final GeocodingProvider geocodingProvider;

    private static final double MIN_DISTANCE_METERS = 30.0;
    private static final double EARTH_RADIUS_METERS = 6371000.0;

    @Override
    @Transactional
    public UserLocation execute(String protectedUserEmail, BigDecimal latitude, BigDecimal longitude, String locationText) {
        log.info("UseCase: Guardando telemetría de ubicación para el PROTECTED [{}]", protectedUserEmail);

        validateProtectedRole(protectedUserEmail);
        UserLocation userLocation = getOrCreateUserLocation(protectedUserEmail);

        String resolvedAddress = resolveAddressText(userLocation, latitude, longitude, locationText);
        userLocation.setLocationText(resolvedAddress);

        userLocation.setLatitude(latitude);
        userLocation.setLongitude(longitude);
        userLocation.setConnected(true);

        log.debug("UseCase: Persistiendo actualización de geolocalización en repositorio.");
        return locationRepository.save(userLocation);
    }

    private void validateProtectedRole(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario emisor no encontrado."));

        if (!Role.PROTECTED.equals(user.getRole())) {
            log.error("Security Violation: Usuario sin rol PROTECTED [{}] intentó emitir coordenadas.", email);
            throw new SecurityException("Acceso denegado: Solo los usuarios protegidos pueden emitir su ubicación.");
        }
    }

    private UserLocation getOrCreateUserLocation(String email) {
        return locationRepository.findByProtectedUserEmail(email)
                .orElseGet(() -> {
                    log.info("UseCase: Primera transmisión detectada para [{}]. Resolviendo vínculo mediante TrustContactRepository...", email);
                    TrustContact contact = trustContactRepository.findByProtectedUser_Email(email)
                            .orElseThrow(() -> new ResourceNotFoundException("No se encontró un vínculo de contacto asignado para este protegido: " + email));

                    return UserLocation.builder().trustContact(contact).build();
                });
    }

    private String resolveAddressText(UserLocation currentLoc, BigDecimal lat, BigDecimal lng, String incomingText) {
        if (incomingText != null && !incomingText.isEmpty()) {
            return incomingText;
        }

        if (isGeocodingRequired(currentLoc, lat, lng)) {
            log.info("UseCase: Distancia significativa detectada (> 30m). Solicitando geocodificación inversa a OpenStreetMap...");
            return geocodingProvider.getAddressFromCoordinates(lat, lng);
        }

        return currentLoc.getLocationText() != null ? currentLoc.getLocationText() : "Ubicación en tiempo real";
    }

    private boolean isGeocodingRequired(UserLocation currentLoc, BigDecimal newLat, BigDecimal newLng) {
        return currentLoc.getLatitude() == null || currentLoc.getLongitude() == null ||
                hasUserMovedSignificantly(currentLoc.getLatitude(), currentLoc.getLongitude(), newLat, newLng);
    }

    private boolean hasUserMovedSignificantly(BigDecimal oldLat, BigDecimal oldLng, BigDecimal newLat, BigDecimal newLng) {
        double dLat = Math.toRadians(newLat.doubleValue() - oldLat.doubleValue());
        double dLng = Math.toRadians(newLng.doubleValue() - oldLng.doubleValue());

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(oldLat.doubleValue())) * Math.cos(Math.toRadians(newLat.doubleValue())) *
                        Math.sin(dLng / 2) * Math.sin(dLng / 2);

        double distance = EARTH_RADIUS_METERS * (2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a)));
        return distance >= MIN_DISTANCE_METERS;
    }
}