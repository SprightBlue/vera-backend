package com.unlam.verabackend.domain.port.in;

import com.unlam.verabackend.presentation.dto.*;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface UserSettingsUseCase {
    @Transactional
    UploadImageResponse uploadUserImage(String email, MultipartFile image) throws IOException;

    @Transactional(readOnly = true)
    ProfileResponse getProfile(String email);

    @Transactional
    ProfileResponse updateProfile(String email, UpdateProfileRequest request);

    @Transactional
    void changeEmail(String currentEmail, ChangeEmailRequest request);

    @Transactional
    void changePassword(String email, ChangePasswordRequest request);

    // Modificá la firma en UserSettingsUseCase.java
    void deleteAccount(String email, DeleteAccountRequest request) throws IOException;

    @Transactional
    void deleteUserImage(String email) throws IOException;
}
