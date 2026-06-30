package com.unlam.verabackend.domain.port.in;

import com.unlam.verabackend.presentation.dto.AuthResponse;
import com.unlam.verabackend.presentation.dto.ChangeEmailRequest;
import com.unlam.verabackend.presentation.dto.ChangePasswordRequest;
import com.unlam.verabackend.presentation.dto.LoginRequest;
import com.unlam.verabackend.presentation.dto.RegisterRequest;
import com.unlam.verabackend.presentation.dto.UploadImageResponse;
import org.springframework.web.multipart.MultipartFile;
import com.unlam.verabackend.presentation.dto.ProfileResponse;
import com.unlam.verabackend.presentation.dto.UpdateProfileRequest;
import com.unlam.verabackend.presentation.dto.ChangeEmailRequest;

import java.io.IOException;

public interface UserUseCase {

        AuthResponse register(RegisterRequest request);

        AuthResponse login(LoginRequest request);

        AuthResponse googleLogin(String credential, String selectedRole);

        void forgotPassword(String email);

        void resetPassword(String token, String newPassword);

        void verifyEmail(String token);

        UploadImageResponse uploadUserImage(String email, MultipartFile image) throws IOException;

        ProfileResponse getProfile(
                        String email);

        ProfileResponse updateProfile(
                        String email,
                        UpdateProfileRequest request);

        void changePassword(
                        String email,
                        ChangePasswordRequest request);

        void changeEmail(
                        String currentEmail,
                        ChangeEmailRequest request);

}