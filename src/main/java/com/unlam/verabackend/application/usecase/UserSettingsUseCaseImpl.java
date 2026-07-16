package com.unlam.verabackend.application.usecase;

import com.unlam.verabackend.domain.exception.ResourceNotFoundException;
import com.unlam.verabackend.domain.port.out.FileCloudProvider;
import com.unlam.verabackend.domain.port.in.UserSettingsUseCase;
import com.unlam.verabackend.infrastructure.entity.User;
import com.unlam.verabackend.infrastructure.repository.UserRepository;
import com.unlam.verabackend.presentation.dto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserSettingsUseCaseImpl implements UserSettingsUseCase {

    private final UserRepository userRepository;
    private final FileCloudProvider cloudinaryFileCloudAdapter;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public UploadImageResponse uploadUserImage(String email, MultipartFile image) throws IOException {
        log.info("Iniciando subida de imagen de perfil para el usuario: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        if (user.getImage() != null) {
            log.debug("Eliminando imagen previa de Cloudinary para el usuario: {}", email);
            cloudinaryFileCloudAdapter.deleteImage(user.getImage());
        }

        String imageUrl = cloudinaryFileCloudAdapter.uploadImage(image, "users");
        user.setImage(imageUrl);
        userRepository.save(user);

        log.info("Imagen de perfil actualizada con éxito para el usuario: {}", email);
        return new UploadImageResponse(user.getEmail(), user.getImage());
    }

    @Override
    @Transactional(readOnly = true)
    public ProfileResponse getProfile(String email) {
        log.debug("Obteniendo perfil para el usuario: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        return mapToProfileResponse(user);
    }

    @Override
    @Transactional
    public ProfileResponse updateProfile(String email, UpdateProfileRequest request) {
        log.info("Actualizando datos de perfil para el usuario: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        user.setFullName(request.getFullName());
        user.setPhone(request.getPhone());
        userRepository.save(user);

        log.info("Perfil actualizado correctamente para el usuario: {}", email);
        return mapToProfileResponse(user);
    }

    @Override
    @Transactional
    public void changeEmail(String currentEmail, ChangeEmailRequest request) {
        log.info("Solicitud de cambio de email para el usuario actual: {}", currentEmail);

        User user = userRepository.findByEmail(currentEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            log.warn("Intento fallido de cambio de email para {}: contraseña incorrecta", currentEmail);
            throw new RuntimeException("La contraseña ingresada no es correcta.");
        }

        String newEmailNormalized = request.getNewEmail().toLowerCase().trim();

        if (userRepository.existsByEmail(newEmailNormalized)) {
            log.warn("No se pudo cambiar el email a {}: ya se encuentra registrado", newEmailNormalized);
            throw new RuntimeException("El correo electrónico ya está registrado en otra cuenta.");
        }

        user.setEmail(newEmailNormalized);
        userRepository.save(user);

        log.info("Email cambiado con éxito. Antiguo: {} -> Nuevo: {}", currentEmail, newEmailNormalized);
    }

    @Override
    @Transactional
    public void changePassword(String email, ChangePasswordRequest request) {
        log.info("Solicitud de cambio de contraseña para el usuario: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            log.warn("Intento fallido de cambio de contraseña para {}: contraseña actual incorrecta", email);
            throw new RuntimeException("La contraseña actual es incorrecta.");
        }

        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            log.warn("Intento de cambio de contraseña fallido para {}: las contraseñas no coinciden", email);
            throw new RuntimeException("Las contraseñas no coinciden.");
        }

        if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
            log.warn("Intento de cambio de contraseña fallido para {}: nueva contraseña idéntica a la anterior", email);
            throw new RuntimeException("La nueva contraseña debe ser distinta a la actual.");
        }

        updatePassword(user, request.getNewPassword());
        log.info("Contraseña actualizada con éxito para el usuario: {}", email);
    }

    private void updatePassword(User user, String newPassword) {
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void deleteAccount(String email, DeleteAccountRequest request) throws IOException {
        log.info("Iniciando proceso de eliminación absoluta de cuenta para: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            log.warn("Intento fallido de eliminación de cuenta para {}: contraseña incorrecta", email);
            throw new RuntimeException("La contraseña ingresada es incorrecta.");
        }

        if (user.getImage() != null) {
            log.debug("Eliminando foto de perfil de Cloudinary antes de borrar al usuario: {}", email);
            cloudinaryFileCloudAdapter.deleteImage(user.getImage());
        }

        userRepository.delete(user);
        log.info("Cuenta removida del sistema exitosamente: {}", email);
    }

    @Override
    @Transactional
    public void deleteUserImage(String email) throws IOException {
        log.info("Solicitud para eliminar imagen de perfil del usuario: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        if (user.getImage() != null) {
            log.debug("Borrando archivo en Cloudinary para el usuario: {}", email);
            cloudinaryFileCloudAdapter.deleteImage(user.getImage());

            user.setImage(null);
            userRepository.save(user);
            log.info("Imagen de perfil eliminada y campo reseteado a null para: {}", email);
        } else {
            log.debug("El usuario {} solicitó borrar su imagen pero no tenía ninguna registrada", email);
        }
    }

    private ProfileResponse mapToProfileResponse(User user) {
        return ProfileResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .role(user.getRole().name())
                .imageUrl(user.getImage())
                .build();
    }
}