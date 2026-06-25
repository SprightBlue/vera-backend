package com.unlam.verabackend.domain.port.out;

import com.unlam.verabackend.domain.model.UserLocation;
import java.util.Optional;

public interface UserLocationRepository {
    UserLocation save(UserLocation userLocation, Long trustContactId);
    Optional<UserLocation> findByTrustContactId(Long trustContactId);
}