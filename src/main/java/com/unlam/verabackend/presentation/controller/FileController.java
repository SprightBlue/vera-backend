package com.unlam.verabackend.presentation.controller;

import com.unlam.verabackend.domain.port.in.TrustContactUseCase;
import com.unlam.verabackend.domain.port.in.UserUseCase;
import com.unlam.verabackend.infrastructure.entity.User;
import com.unlam.verabackend.presentation.dto.UploadImageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/v1/files")
@RequiredArgsConstructor
public class FileController {

    private final UserUseCase userUseCase;
    private final TrustContactUseCase trustContactUseCase;

    @PutMapping("/upload-user-image")
    public ResponseEntity<UploadImageResponse> uploadUserImage(@RequestParam("email") String email,
                                                           @RequestParam("image") MultipartFile image) throws IOException {
        UploadImageResponse updatedUserImage = userUseCase.uploadUserImage(email, image);
        return ResponseEntity.ok(updatedUserImage);
    }

    @PatchMapping("/upload-protected-person-image")
    public ResponseEntity<String> uploadProtectedPersonImage(@RequestParam("image") MultipartFile image) throws IOException {
        String imageUrl = trustContactUseCase.uploadProtectedPersonImage(image);
        return ResponseEntity.ok(imageUrl);
    }

    @DeleteMapping("/delete-user-image/{id}")
    public ResponseEntity<Void> deleteUserImage(@PathVariable Long id) {
        userUseCase.deleteUserImage(id);
        return ResponseEntity.noContent().build();
    }
}