package com.unlam.verabackend.domain.port.in;

import com.unlam.verabackend.domain.model.UserLocation;

public interface GetUserLocationUseCase {
    UserLocation execute(Long trustContactId, String carerEmail);
}