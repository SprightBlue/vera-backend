package com.unlam.verabackend.domain.port.out;

import java.math.BigDecimal;

public interface GeoLocationProvider {
    String getAddressFromCoords(BigDecimal lat, BigDecimal lon);
}