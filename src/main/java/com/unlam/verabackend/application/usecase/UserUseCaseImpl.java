package com.unlam.verabackend.application.usecase;

import com.unlam.verabackend.application.service.CloudinaryService;
import com.unlam.verabackend.application.service.JwtService;
import com.unlam.verabackend.presentation.dto.AuthResponse;
import com.unlam.verabackend.presentation.dto.ChangeEmailRequest;
import com.unlam.verabackend.presentation.dto.ChangePasswordRequest;
import com.unlam.verabackend.presentation.dto.LoginRequest;
import com.unlam.verabackend.presentation.dto.RegisterRequest;
import java.util.List;

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
import com.unlam.verabackend.presentation.dto.ProfileResponse;
import com.unlam.verabackend.presentation.dto.UpdateProfileRequest;
import com.unlam.verabackend.infrastructure.repository.TrustContactRepository;
import com.unlam.verabackend.infrastructure.entity.TrustContact;
import com.unlam.verabackend.domain.model.Role;

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
        private final TrustContactRepository trustContactRepository;

        @Value("${google.client-id}")
        private String googleClientId;

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
                user.setRole(Role.valueOf(request.getRole()));
                user.setEnabled(false);

                userRepository.save(user);

                String tokenDeEmail = java.util.UUID.randomUUID().toString();
                VerificationToken verificationToken = new VerificationToken(tokenDeEmail, user);
                verificationTokenRepository.save(verificationToken);

                emailService.sendVerificationEmail(user.getEmail(), tokenDeEmail);

                return new AuthResponse(
                                user.getId(),
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
                return new AuthResponse(
                                user.getId(),
                                token,
                                user.getEmail(),
                                user.getFullName(),
                                user.getRole().name(),
                                user.getImage());
        }

        @Override
        public AuthResponse googleLogin(
                        String credential, String selectedRole) {

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
                                String roleToSet = (selectedRole != null && !selectedRole.isBlank()) ? selectedRole
                                                : "CARER";

                                user.setRole(Role.valueOf(roleToSet));

                                user.setEnabled(true);

                                user = userRepository.save(user);
                        }

                        String token = jwtService.generateToken(user);

                        return new AuthResponse(
                                        user.getId(),
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

                updatePassword(
                                user,
                                newPassword);

                resetToken.setUsed(true);

                tokenRepository.save(resetToken);
        }

        @Override
        public void changePassword(
                        String email,
                        ChangePasswordRequest request) {

                User user = userRepository.findByEmail(email)
                                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

                if (!passwordEncoder.matches(
                                request.getCurrentPassword(),
                                user.getPassword())) {
                        throw new RuntimeException(
                                        "La contraseña actual es incorrecta");
                }

                if (!request.getNewPassword().equals(
                                request.getConfirmPassword())) {
                        throw new RuntimeException(
                                        "Las contraseñas no coinciden");
                }

                if (passwordEncoder.matches(
                                request.getNewPassword(),
                                user.getPassword())) {
                        throw new RuntimeException(
                                        "La nueva contraseña debe ser distinta a la actual");
                }

                updatePassword(
                                user,
                                request.getNewPassword());

        }

        private void updatePassword(
                        User user,
                        String newPassword) {

                user.setPassword(
                                passwordEncoder.encode(newPassword));

                userRepository.save(user);

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
                                user.getImage());
        }

        @Override
        public ProfileResponse getProfile(String email) {

                User user = userRepository.findByEmail(email)
                                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

                return ProfileResponse.builder()
                                .id(user.getId())
                                .fullName(user.getFullName())
                                .email(user.getEmail())
                                .phone(user.getPhone())
                                .country(user.getCountry())
                                .role(user.getRole().name())
                                .imageUrl(user.getImage())
                                .build();
        }

        @Override
        public ProfileResponse updateProfile(
                        String email,
                        UpdateProfileRequest request) {

                User user = userRepository.findByEmail(email)
                                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

                user.setFullName(request.getFullName());
                user.setPhone(request.getPhone());

                // Por ahora reutilizamos timezone para guardar el país
                user.setCountry(request.getCountry());

                userRepository.save(user);

                return ProfileResponse.builder()
                                .id(user.getId())
                                .fullName(user.getFullName())
                                .email(user.getEmail())
                                .phone(user.getPhone())
                                .country(user.getCountry())
                                .role(user.getRole().name())
                                .imageUrl(user.getImage())
                                .build();
        }

        @Override
        public void changeEmail(
                        String currentEmail,
                        ChangeEmailRequest request) {

                User user = userRepository.findByEmail(currentEmail)
                                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

                if (!passwordEncoder.matches(
                                request.getPassword(),
                                user.getPassword())) {

                        throw new RuntimeException(
                                        "La contraseña es incorrecta");

                }

                if (userRepository.existsByEmail(
                                request.getNewEmail())) {

                        throw new RuntimeException(
                                        "Ese correo ya está registrado");

                }

                user.setEmail(
                                request.getNewEmail());

                userRepository.save(user);

        }

        @Override
        public void deleteAccount(String email) {

                User user = userRepository.findByEmail(email)
                                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

                if (user.getRole() == Role.CARER) {

                        List<TrustContact> contacts = trustContactRepository.findByCarerId(user.getId());

                        if (!contacts.isEmpty()) {

                                throw new RuntimeException(
                                                "No puedes eliminar tu cuenta porque todavía tienes personas protegidas.");

                        }

                }

                if (user.getRole() == Role.PROTECTED) {

                        List<TrustContact> contacts = trustContactRepository.findByProtectedUserId(user.getId());

                        if (!contacts.isEmpty()) {

                                throw new RuntimeException(
                                                "No puedes eliminar tu cuenta porque todavía estás asociado a un cuidador.");

                        }

                }

                userRepository.delete(user);

        }

    @Override
    public void deleteUserImage(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        user.setId(null);
        userRepository.save(user);
    }
}