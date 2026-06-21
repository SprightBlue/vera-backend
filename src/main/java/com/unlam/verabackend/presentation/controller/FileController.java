package com.unlam.verabackend.presentation.controller;

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

    @PutMapping("/upload-image")
    public ResponseEntity<UploadImageResponse> uploadImage(@RequestParam("email") String email,
                                                           @RequestParam("image") MultipartFile image) throws IOException {
        UploadImageResponse updatedUserImage = userUseCase.uploadUserImage(email, image);
        return ResponseEntity.ok(updatedUserImage);
    }
}