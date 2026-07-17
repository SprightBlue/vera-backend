package com.unlam.verabackend.domain.port.in;

import com.unlam.verabackend.presentation.dto.AuthResponse;
import com.unlam.verabackend.presentation.dto.LoginRequest;
import com.unlam.verabackend.presentation.dto.RegisterRequest;
import org.springframework.transaction.annotation.Transactional;

public interface UserUseCase {
        @Transactional
        AuthResponse register(RegisterRequest request) throws Exception;

        @Transactional
        AuthResponse login(LoginRequest request);

        @Transactional
        AuthResponse googleLogin(String credential, String selectedRole);

        @Transactional
        void forgotPassword(String email) throws Exception;

        @Transactional
        void resetPassword(String token, String newPassword);

        @Transactional
        void verifyEmail(String token);
}