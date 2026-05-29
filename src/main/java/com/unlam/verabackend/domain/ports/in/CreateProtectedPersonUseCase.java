package com.unlam.verabackend.domain.ports.in;

import com.unlam.verabackend.domain.model.UserCaregiver;
import com.unlam.verabackend.infrastructure.dto.CreateProtectedPersonRequest;

import java.util.List;

public interface CreateProtectedPersonUseCase {

    void execute(
            Long userId,
            CreateProtectedPersonRequest request
    );

    List<UserCaregiver> getByUserId(Long userId);

}