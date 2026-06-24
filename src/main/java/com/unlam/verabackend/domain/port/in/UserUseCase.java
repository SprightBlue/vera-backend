package com.unlam.verabackend.domain.port.in;

import com.unlam.verabackend.presentation.dto.AuthResponse;
import com.unlam.verabackend.presentation.dto.LoginRequest;
import com.unlam.verabackend.presentation.dto.RegisterRequest;
import com.unlam.verabackend.presentation.dto.UploadImageResponse;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface UserUseCase {

    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);

    AuthResponse googleLogin(String credential, String selectedRole);

    void forgotPassword(String email);

    void resetPassword(String token, String newPassword);

    void verifyEmail(String token);

    UploadImageResponse uploadUserImage(String email, MultipartFile image) throws IOException;
    
}