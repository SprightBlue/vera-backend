package com.unlam.verabackend.services.Interface;

import com.unlam.verabackend.dto.AuthResponse;
import com.unlam.verabackend.dto.LoginRequest;
import com.unlam.verabackend.dto.RegisterRequest;

public interface UserService {

    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);
}