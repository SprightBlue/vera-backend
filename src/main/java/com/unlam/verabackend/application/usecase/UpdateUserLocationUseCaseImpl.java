package com.unlam.verabackend.application.usecase;

import com.unlam.verabackend.domain.exception.ResourceNotFoundException;
import com.unlam.verabackend.domain.model.UserLocation;
import com.unlam.verabackend.domain.port.in.UpdateUserLocationUseCase;
import com.unlam.verabackend.domain.port.out.GeocodingProvider;
import com.unlam.verabackend.domain.port.out.UserLocationRepository;
import com.unlam.verabackend.infrastructure.entity.TrustContact;
import com.unlam.verabackend.infrastructure.repository.TrustContactRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Slf4j
public class UpdateUserLocationUseCaseImpl implements UpdateUserLocationUseCase {

    private final UserLocationRepository locationRepository;
    private final TrustContactRepository trustContactRepository;
    private final GeocodingProvider geocodingProvider;

    private static final double MIN_DISTANCE_METERS = 30.0;

    @Override
    @Transactional
    public UserLocation execute(String protectedUserEmail, BigDecimal latitude, BigDecimal longitude, String locationText) {
        log.info("UseCase: Solicitud de actualización de ubicación para [{}] -> Lat: {}, Lng: {}", protectedUserEmail, latitude, longitude);

        UserLocation userLocation = locationRepository.findByProtectedUserEmail(protectedUserEmail)
                .orElseGet(() -> {
                    log.info("UseCase: No se encontró registro previo de ubicación para [{}]. Creando uno nuevo.", protectedUserEmail);
                    TrustContact contact = trustContactRepository.findByProtectedUser_Email(protectedUserEmail)
                            .orElseThrow(() -> {
                                log.error("UseCase Error: Relación de confianza no encontrada para el email: [{}]", protectedUserEmail);
                                return new ResourceNotFoundException("No se encontró una relación de confianza activa para el email: " + protectedUserEmail);
                            });
                    return UserLocation.builder().trustContact(contact).build();
                });

        if (locationText == null || locationText.isEmpty()) {

            if (userLocation.getLatitude() == null || userLocation.getLongitude() == null ||
                    hasUserMovedSignificantly(userLocation.getLatitude(), userLocation.getLongitude(), latitude, longitude)) {

                log.info("UseCase [OSM]: El usuario se movió más de {} metros o es su primer registro. Consultando OpenStreetMap...", MIN_DISTANCE_METERS);
                String address = geocodingProvider.getAddressFromCoordinates(latitude, longitude);
                userLocation.setLocationText(address);
            } else {
                log.debug("UseCase [OSM]: Movimiento insignificante. Omitiendo llamada externa a OSM y reutilizando dirección.");
                if (userLocation.getLocationText() == null) {
                    userLocation.setLocationText("Ubicación en tiempo real");
                }
            }
        } else {
            log.info("UseCase: Se utilizará el texto de ubicación provisto explícitamente: '{}'", locationText);
            userLocation.setLocationText(locationText);
        }

        userLocation.setLatitude(latitude);
        userLocation.setLongitude(longitude);
        userLocation.setConnected(true);

        log.info("UseCase: Ubicación persistida de manera exitosa para [{}].", protectedUserEmail);
        return locationRepository.save(userLocation);
    }

    private boolean hasUserMovedSignificantly(BigDecimal oldLat, BigDecimal oldLng, BigDecimal newLat, BigDecimal newLng) {
        double earthRadius = 6371000;

        double dLat = Math.toRadians(newLat.doubleValue() - oldLat.doubleValue());
        double dLng = Math.toRadians(newLng.doubleValue() - oldLng.doubleValue());

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(oldLat.doubleValue())) * Math.cos(Math.toRadians(newLat.doubleValue())) *
                        Math.sin(dLng / 2) * Math.sin(dLng / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = earthRadius * c;

        log.debug("UseCase [Haversine]: Distancia calculada: {} metros.", distance);
        return distance >= MIN_DISTANCE_METERS;
    }
}