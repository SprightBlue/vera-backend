package com.unlam.verabackend.domain.port.in;

import com.unlam.verabackend.domain.model.UserLocation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

public interface UpdateUserLocationUseCase {
    @Transactional
    UserLocation execute(String protectedUserEmail, BigDecimal latitude, BigDecimal longitude, String locationText);
}