package com.unlam.verabackend.domain.ports.inbound;

import com.unlam.verabackend.presentation.dto.response.AuthResponse;
import com.unlam.verabackend.presentation.dto.request.LoginRequest;
import com.unlam.verabackend.presentation.dto.request.RegisterRequest;

public interface UserService {

    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);
}
