package com.unlam.verabackend.domain.ports.in;

import com.unlam.verabackend.presentation.dto.AuthResponse;
import com.unlam.verabackend.presentation.dto.LoginRequest;
import com.unlam.verabackend.presentation.dto.RegisterRequest;

public interface UserUseCase {

    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);
}