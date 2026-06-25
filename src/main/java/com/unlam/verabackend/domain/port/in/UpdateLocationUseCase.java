package com.unlam.verabackend.domain.port.in;

import com.unlam.verabackend.domain.model.UserLocation;
import java.math.BigDecimal;

public interface UpdateLocationUseCase {
    UserLocation execute(String email, BigDecimal lat, BigDecimal lon);
}