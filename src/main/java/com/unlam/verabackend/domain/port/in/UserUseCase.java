package com.unlam.verabackend.domain.port.in;

import com.unlam.verabackend.presentation.dto.AuthResponse;
import com.unlam.verabackend.presentation.dto.LoginRequest;
import com.unlam.verabackend.presentation.dto.RegisterRequest;

public interface UserUseCase {

    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);

    void forgotPassword(String email);

    void resetPassword(String token, String newPassword);
}