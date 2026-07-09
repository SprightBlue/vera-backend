package com.unlam.verabackend.domain.port.out;

import com.unlam.verabackend.domain.model.UserLocation;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserLocationRepository {
    UserLocation save(UserLocation userLocation);
    Optional<UserLocation> findById(UUID id);
    Optional<UserLocation> findByProtectedUserEmail(String email);
    List<UserLocation> findTop3LastConnectedByCarerEmail(String carerEmail);
    long countConnectedUsersByCarerEmail(String carerEmail);
}