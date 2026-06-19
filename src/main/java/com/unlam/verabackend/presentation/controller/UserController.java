package com.unlam.verabackend.presentation.controller;

import com.unlam.verabackend.application.service.CloudinaryService;
import com.unlam.verabackend.infrastructure.entity.User;
import com.unlam.verabackend.infrastructure.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {


    private final CloudinaryService cloudinaryService;
    private final UserRepository userRepository;


    public UserController(CloudinaryService cloudinaryService, UserRepository userRepository){
        this.cloudinaryService = cloudinaryService;
        this.userRepository = userRepository;
    }

    @PutMapping("/image")
    public ResponseEntity<?> uploadUserImage(@RequestParam("email") String email,
                                             @RequestParam("image") MultipartFile image) throws IOException {

        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        String imageUrl = cloudinaryService.uploadImage(image);
        user.setImageUrl(imageUrl);
        userRepository.save(user);

        return ResponseEntity.ok(user);
    }
}