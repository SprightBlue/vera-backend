package com.unlam.verabackend.domain.port.in;

import com.unlam.verabackend.domain.model.UserLocation;
import org.springframework.transaction.annotation.Transactional;

public interface GetUserLocationUseCase {
    @Transactional
    UserLocation execute(Long trustContactId, String carerEmail);
}