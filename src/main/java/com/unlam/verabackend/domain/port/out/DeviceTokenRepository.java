package com.unlam.verabackend.domain.port.out;

import com.unlam.verabackend.domain.model.DeviceToken;
import com.unlam.verabackend.infrastructure.entity.User;

import java.util.List;

public interface DeviceTokenRepository {
    DeviceToken saveOrUpdate(User user, String token, String platform);
    List<DeviceToken> findActiveByUserEmail(String email);
    void deactivateToken(String token);
}
