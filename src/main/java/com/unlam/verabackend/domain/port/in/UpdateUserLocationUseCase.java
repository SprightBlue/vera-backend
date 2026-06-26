package com.unlam.verabackend.domain.port.in;

import com.unlam.verabackend.domain.model.UserLocation;
import java.math.BigDecimal;

public interface UpdateUserLocationUseCase {
    UserLocation execute(String protectedUserEmail, BigDecimal latitude, BigDecimal longitude, String locationText);
}