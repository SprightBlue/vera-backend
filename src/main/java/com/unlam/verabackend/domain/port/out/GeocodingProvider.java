package com.unlam.verabackend.domain.port.out;

import java.math.BigDecimal;

public interface GeocodingProvider {
    String getAddressFromCoordinates(BigDecimal latitude, BigDecimal longitude);
}