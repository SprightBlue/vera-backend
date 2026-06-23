package com.unlam.verabackend.application.usecase;

import com.unlam.verabackend.application.service.CloudinaryService;
import com.unlam.verabackend.application.service.JwtService;
import com.unlam.verabackend.presentation.dto.AuthResponse;
import com.unlam.verabackend.presentation.dto.LoginRequest;
import com.unlam.verabackend.presentation.dto.RegisterRequest;

import com.unlam.verabackend.presentation.dto.UploadImageResponse;
import jakarta.transaction.Transactional;

import com.unlam.verabackend.domain.model.Role;
import com.unlam.verabackend.infrastructure.entity.User;
import com.unlam.verabackend.infrastructure.entity.VerificationToken;
import com.unlam.verabackend.infrastructure.repository.UserRepository;
import com.unlam.verabackend.infrastructure.repository.VerificationTokenRepository;
import com.unlam.verabackend.domain.port.in.UserUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.unlam.verabackend.application.service.EmailService;
import com.unlam.verabackend.infrastructure.entity.PasswordResetToken;
import com.unlam.verabackend.infrastructure.repository.PasswordResetTokenRepository;

import java.io.IOException;
import java.util.Optional;
import java.util.Collections;

import org.springframework.beans.factory.annotation.Value;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserUseCaseImpl implements UserUseCase {

        private final UserRepository userRepository;
        private final PasswordEncoder passwordEncoder;
        private final JwtService jwtService;
        private final AuthenticationManager authenticationManager;
        private final PasswordResetTokenRepository tokenRepository;
        private final VerificationTokenRepository verificationTokenRepository;
        private final EmailService emailService;
        private final CloudinaryService cloudinaryService;

        @Override
        public AuthResponse register(RegisterRequest request) {

                if (request.getAcceptedTerms() == null
                                || !request.getAcceptedTerms()) {

                        throw new IllegalArgumentException(
                                        "Debe aceptar los términos y condiciones");
                }

                if (userRepository.existsByEmail(request.getEmail())) {
                        throw new RuntimeException("El correo electrónico ya está registrado");
                }

                User user = new User();
                user.setFullName(request.getFullName());
                user.setEmail(request.getEmail());
                user.setPassword(passwordEncoder.encode(request.getPassword()));
                user.setRole(Role.ROLE_USER);
                user.setEnabled(false);

                userRepository.save(user);

                String tokenDeEmail = java.util.UUID.randomUUID().toString();
                VerificationToken verificationToken = new VerificationToken(tokenDeEmail, user);
                verificationTokenRepository.save(verificationToken);

                emailService.sendVerificationEmail(user.getEmail(), tokenDeEmail);

                return new AuthResponse(
                                null,
                                user.getEmail(),
                                user.getFullName(),
                                user.getRole().name(),
                                user.getImage());
        }

        @Override
        public AuthResponse login(LoginRequest request) {
                authenticationManager.authenticate(
                                new UsernamePasswordAuthenticationToken(
                                                request.getEmail(),
                                                request.getPassword()));

                User user = userRepository.findByEmail(request.getEmail())
                                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

                String token = jwtService.generateToken(user);
                return new AuthResponse(token, user.getEmail(), user.getFullName(), user.getRole().name(), user.getImage());
        }

        @Override
        public AuthResponse googleLogin(
                        String credential) {

                try {

                        GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                                        GoogleNetHttpTransport.newTrustedTransport(),
                                        GsonFactory.getDefaultInstance())
                                        .setAudience(
                                                        Collections.singletonList(
                                                                        googleClientId))
                                        .build();

                        GoogleIdToken idToken = verifier.verify(credential);

                        if (idToken == null) {
                                throw new RuntimeException(
                                                "Token Google inválido");
                        }

                        GoogleIdToken.Payload payload = idToken.getPayload();

                        String email = payload.getEmail();

                        String fullName = (String) payload.get("name");

                        Optional<User> existingUser = userRepository.findByEmail(email);

                        User user;

                        if (existingUser.isPresent()) {

                                user = existingUser.get();

                        } else {

                                user = new User();

                                user.setEmail(email);

                                user.setFullName(fullName);

                                user.setPassword(
                                                passwordEncoder.encode(
                                                                UUID.randomUUID().toString()));

                                user.setRole(Role.ROLE_USER);

                                user.setEnabled(true);

                                user = userRepository.save(user);
                        }

                        String token = jwtService.generateToken(user);

                        return new AuthResponse(
                                        token,
                                        user.getEmail(),
                                        user.getFullName(),
                                        user.getRole().name(),
                                        user.getImage());

                } catch (Exception e) {

                        throw new RuntimeException(
                                        "Error validando usuario Google",
                                        e);
                }
        }

        @Value("${google.client-id}")
        private String googleClientId;

        @Override
        public void forgotPassword(String email) {

                User user = userRepository.findByEmail(email)
                                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

                String token = UUID.randomUUID().toString();

                PasswordResetToken resetToken = new PasswordResetToken();

                resetToken.setToken(token);
                resetToken.setUser(user);
                resetToken.setExpiresAt(
                                LocalDateTime.now().plusHours(1));

                tokenRepository.save(resetToken);

                emailService.sendPasswordResetEmail(
                                user.getEmail(),
                                token);
        }

        @Override
        public void resetPassword(
                        String token,
                        String newPassword) {

                PasswordResetToken resetToken = tokenRepository.findByToken(token)
                                .orElseThrow(() -> new RuntimeException(
                                                "Token inválido"));

                if (resetToken.isUsed()) {
                        throw new RuntimeException(
                                        "Token ya utilizado");
                }

                if (resetToken.getExpiresAt()
                                .isBefore(LocalDateTime.now())) {
                        throw new RuntimeException(
                                        "Token expirado");
                }

                User user = resetToken.getUser();

                user.setPassword(
                                passwordEncoder.encode(newPassword));

                userRepository.save(user);

                resetToken.setUsed(true);

                tokenRepository.save(resetToken);
        }

        @Override
        @Transactional
        public void verifyEmail(String token) {

                VerificationToken verificationToken = verificationTokenRepository.findByToken(token)
                                .orElseThrow(() -> new RuntimeException("Token inválido o no encontrado"));

                if (verificationToken.getExpiryDate().isBefore(java.time.LocalDateTime.now())) {
                        throw new RuntimeException("El enlace de verificación ha expirado");
                }

                User user = verificationToken.getUser();
                user.setEnabled(true);
                userRepository.save(user);

                verificationTokenRepository.delete(verificationToken);
        }

        @Override
        public UploadImageResponse uploadUserImage(String email, MultipartFile image) throws IOException {

            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

            String imageUrl = cloudinaryService.uploadImage(image, "users");

            user.setImage(imageUrl);
            userRepository.save(user);

            return new UploadImageResponse(
                    user.getEmail(),
                    user.getImage()
            );
        }

}