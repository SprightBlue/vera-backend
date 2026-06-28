package com.unlam.verabackend.domain.port.out;

import com.unlam.verabackend.domain.model.UserLocation;
import java.util.Optional;
import java.util.UUID;

public interface UserLocationRepository {
    UserLocation save(UserLocation userLocation);
    Optional<UserLocation> findById(UUID id);
    Optional<UserLocation> findByTrustContactId(Long trustContactId);
    Optional<UserLocation> findByProtectedUserEmail(String email);
}