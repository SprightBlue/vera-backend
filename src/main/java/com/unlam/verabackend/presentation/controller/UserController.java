package com.unlam.verabackend.presentation.controller;

import com.unlam.verabackend.domain.port.in.UserUseCase;
import com.unlam.verabackend.infrastructure.entity.User;
import com.unlam.verabackend.presentation.dto.ChangeEmailRequest;
import com.unlam.verabackend.presentation.dto.ChangePasswordRequest;
import com.unlam.verabackend.presentation.dto.ProfileResponse;
import com.unlam.verabackend.presentation.dto.UpdateProfileRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import com.unlam.verabackend.presentation.dto.ChangePasswordRequest;
import com.unlam.verabackend.presentation.dto.ChangeEmailRequest;

@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
public class UserController {

        private final UserUseCase userUseCase;

        @GetMapping("/profile")
        public ResponseEntity<ProfileResponse> getProfile(
                        @AuthenticationPrincipal User user) {

                return ResponseEntity.ok(
                                userUseCase.getProfile(user.getEmail()));
        }

        @PutMapping("/profile")
        public ResponseEntity<ProfileResponse> updateProfile(
                        @AuthenticationPrincipal User user,
                        @Valid @RequestBody UpdateProfileRequest request) {

                return ResponseEntity.ok(
                                userUseCase.updateProfile(
                                                user.getEmail(),
                                                request));
        }

        @PutMapping("/password")
        public ResponseEntity<Void> changePassword(
                        @AuthenticationPrincipal User user,
                        @Valid @RequestBody ChangePasswordRequest request) {

                userUseCase.changePassword(
                                user.getEmail(),
                                request);

                return ResponseEntity.ok().build();
        }

        @PutMapping("/email")
        public ResponseEntity<Void> changeEmail(
                        @AuthenticationPrincipal User user,
                        @Valid @RequestBody ChangeEmailRequest request) {

                userUseCase.changeEmail(
                                user.getEmail(),
                                request);

                return ResponseEntity.ok().build();

        }

}