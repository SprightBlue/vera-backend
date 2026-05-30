package com.unlam.verabackend.presentation.controller;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.unlam.verabackend.domain.ports.inbound.UserService;
import com.unlam.verabackend.presentation.dto.request.LoginRequest;
import com.unlam.verabackend.presentation.dto.request.RegisterRequest;
import com.unlam.verabackend.presentation.dto.response.AuthResponse;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(
        @Valid @RequestBody RegisterRequest request
    ) {
        AuthResponse response = userService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
        @Valid @RequestBody LoginRequest request
    ) {
        AuthResponse response = userService.login(request);
        return ResponseEntity.ok(response); 
    }
}