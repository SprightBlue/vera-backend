package com.unlam.verabackend.application.usecase;

import com.unlam.verabackend.domain.exception.ResourceNotFoundException;
import com.unlam.verabackend.domain.model.Role;
import com.unlam.verabackend.domain.port.out.FileCloudProvider;
import com.unlam.verabackend.infrastructure.entity.User;
import com.unlam.verabackend.infrastructure.repository.UserRepository;
import com.unlam.verabackend.presentation.dto.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Pruebas Unitarias para UserSettingsUseCaseImpl")
class UserSettingsUseCaseImplTest {

    @Mock private UserRepository userRepository;
    @Mock private FileCloudProvider cloudinaryFileCloudAdapter;
    @Mock private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserSettingsUseCaseImpl userSettingsUseCase;

    private String email;
    private User baseUser;

    @BeforeEach
    void setUp() {
        email = "test@unlam.edu.ar";
        baseUser = User.builder()
                .id(1L)
                .fullName("Juan Pérez")
                .email(email)
                .phone("+541198765432")
                .role(Role.CARER)
                .password("encoded_password")
                .image("http://cloudinary.com/old_image.png")
                .build();
    }

    @Nested
    @DisplayName("Pruebas para uploadUserImage")
    class UploadUserImageTests {

        @Test
        @DisplayName("Debería subir imagen correctamente reemplazando la existente")
        void uploadUserImage_WithPreviousImage_ShouldDeleteOldAndUploadNew() throws IOException {
            // Arrange
            MultipartFile mockFile = new MockMultipartFile("image", "new.png", "image/png", new byte[]{1, 2, 3});
            String newImageUrl = "http://cloudinary.com/new_image.png";

            when(userRepository.findByEmail(email)).thenReturn(Optional.of(baseUser));
            when(cloudinaryFileCloudAdapter.uploadImage(mockFile, "users")).thenReturn(newImageUrl);

            // Act
            UploadImageResponse response = userSettingsUseCase.uploadUserImage(email, mockFile);

            // Assert
            assertNotNull(response);
            assertEquals(email, response.getEmail());
            assertEquals(newImageUrl, response.getImage());
            assertEquals(newImageUrl, baseUser.getImage());

            verify(cloudinaryFileCloudAdapter, times(1)).deleteImage("http://cloudinary.com/old_image.png");
            verify(userRepository, times(1)).save(baseUser);
        }

        @Test
        @DisplayName("Debería subir imagen sin intentar borrar si no existía previa")
        void uploadUserImage_WithoutPreviousImage_ShouldOnlyUpload() throws IOException {
            // Arrange
            baseUser.setImage(null);
            MultipartFile mockFile = new MockMultipartFile("image", "new.png", "image/png", new byte[]{1, 2, 3});
            String newImageUrl = "http://cloudinary.com/new_image.png";

            when(userRepository.findByEmail(email)).thenReturn(Optional.of(baseUser));
            when(cloudinaryFileCloudAdapter.uploadImage(mockFile, "users")).thenReturn(newImageUrl);

            // Act
            UploadImageResponse response = userSettingsUseCase.uploadUserImage(email, mockFile);

            // Assert
            assertNotNull(response);
            assertEquals(newImageUrl, response.getImage());
            verify(cloudinaryFileCloudAdapter, never()).deleteImage(anyString());
            verify(userRepository, times(1)).save(baseUser);
        }

        @Test
        @DisplayName("Debería lanzar ResourceNotFoundException si el usuario no existe")
        void uploadUserImage_UserNotFound_ShouldThrowException() {
            // Arrange
            MultipartFile mockFile = new MockMultipartFile("image", "new.png", "image/png", new byte[]{1, 2, 3});
            when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(ResourceNotFoundException.class, () ->
                    userSettingsUseCase.uploadUserImage(email, mockFile)
            );
            verifyNoInteractions(cloudinaryFileCloudAdapter);
        }
    }

    @Nested
    @DisplayName("Pruebas para getProfile")
    class GetProfileTests {

        @Test
        @DisplayName("Debería retornar ProfileResponse exitosamente")
        void getProfile_UserExists_ShouldReturnProfileResponse() {
            // Arrange
            when(userRepository.findByEmail(email)).thenReturn(Optional.of(baseUser));

            // Act
            ProfileResponse response = userSettingsUseCase.getProfile(email);

            // Assert
            assertNotNull(response);
            assertEquals(baseUser.getId(), response.getId());
            assertEquals(baseUser.getFullName(), response.getFullName());
            assertEquals(baseUser.getEmail(), response.getEmail());
            assertEquals(baseUser.getPhone(), response.getPhone());
            assertEquals(baseUser.getRole().name(), response.getRole());
            assertEquals(baseUser.getImage(), response.getImageUrl());
        }

        @Test
        @DisplayName("Debería lanzar ResourceNotFoundException si el usuario no existe")
        void getProfile_UserNotFound_ShouldThrowException() {
            // Arrange
            when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(ResourceNotFoundException.class, () -> userSettingsUseCase.getProfile(email));
        }
    }

    @Nested
    @DisplayName("Pruebas para updateProfile")
    class UpdateProfileTests {

        @Test
        @DisplayName("Debería actualizar campos de perfil correctamente")
        void updateProfile_UserExists_ShouldUpdateFields() {
            // Arrange
            UpdateProfileRequest request = new UpdateProfileRequest();
            request.setFullName("Juan Carlos Pérez");
            request.setPhone("+541199999999");

            when(userRepository.findByEmail(email)).thenReturn(Optional.of(baseUser));

            // Act
            ProfileResponse response = userSettingsUseCase.updateProfile(email, request);

            // Assert
            assertNotNull(response);
            assertEquals("Juan Carlos Pérez", baseUser.getFullName());
            assertEquals("+541199999999", baseUser.getPhone());
            assertEquals("Juan Carlos Pérez", response.getFullName());
            verify(userRepository, times(1)).save(baseUser);
        }
    }

    @Nested
    @DisplayName("Pruebas para changeEmail")
    class ChangeEmailTests {

        private ChangeEmailRequest request;

        @BeforeEach
        void setUp() {
            request = new ChangeEmailRequest();
            request.setNewEmail("NUEVO@unlam.edu.ar");
            request.setPassword("correct_password");
        }

        @Test
        @DisplayName("Debería actualizar el email de forma normalizada")
        void changeEmail_ValidData_ShouldUpdateAndNormalizeEmail() {
            // Arrange
            when(userRepository.findByEmail(email)).thenReturn(Optional.of(baseUser));
            when(passwordEncoder.matches(request.getPassword(), baseUser.getPassword())).thenReturn(true);
            when(userRepository.existsByEmail("nuevo@unlam.edu.ar")).thenReturn(false);

            // Act
            userSettingsUseCase.changeEmail(email, request);

            // Assert
            assertEquals("nuevo@unlam.edu.ar", baseUser.getEmail());
            verify(userRepository, times(1)).save(baseUser);
        }

        @Test
        @DisplayName("Debería lanzar RuntimeException si la contraseña actual es errónea")
        void changeEmail_IncorrectPassword_ShouldThrowRuntimeException() {
            // Arrange
            when(userRepository.findByEmail(email)).thenReturn(Optional.of(baseUser));
            when(passwordEncoder.matches(request.getPassword(), baseUser.getPassword())).thenReturn(false);

            // Act & Assert
            RuntimeException ex = assertThrows(RuntimeException.class, () ->
                    userSettingsUseCase.changeEmail(email, request)
            );
            assertEquals("La contraseña ingresada no es correcta.", ex.getMessage());
            verify(userRepository, never()).save(any(User.class));
        }

        @Test
        @DisplayName("Debería lanzar RuntimeException si el nuevo email ya existe en otra cuenta")
        void changeEmail_EmailAlreadyExists_ShouldThrowRuntimeException() {
            // Arrange
            when(userRepository.findByEmail(email)).thenReturn(Optional.of(baseUser));
            when(passwordEncoder.matches(request.getPassword(), baseUser.getPassword())).thenReturn(true);
            when(userRepository.existsByEmail("nuevo@unlam.edu.ar")).thenReturn(true);

            // Act & Assert
            RuntimeException ex = assertThrows(RuntimeException.class, () ->
                    userSettingsUseCase.changeEmail(email, request)
            );
            assertEquals("El correo electrónico ya está registrado en otra cuenta.", ex.getMessage());
            verify(userRepository, never()).save(any(User.class));
        }
    }

    @Nested
    @DisplayName("Pruebas para changePassword")
    class ChangePasswordTests {

        private ChangePasswordRequest request;

        @BeforeEach
        void setUp() {
            request = new ChangePasswordRequest();
            request.setCurrentPassword("correct_password");
            request.setNewPassword("NewSecurePass123!");
            request.setConfirmPassword("NewSecurePass123!");
        }

        @Test
        @DisplayName("Debería cambiar contraseña correctamente")
        void changePassword_ValidFlow_ShouldUpdatePassword() {
            // Arrange
            when(userRepository.findByEmail(email)).thenReturn(Optional.of(baseUser));
            when(passwordEncoder.matches(request.getCurrentPassword(), baseUser.getPassword())).thenReturn(true);
            when(passwordEncoder.matches(request.getNewPassword(), baseUser.getPassword())).thenReturn(false);
            when(passwordEncoder.encode(request.getNewPassword())).thenReturn("new_hashed_password");

            // Act
            userSettingsUseCase.changePassword(email, request);

            // Assert
            assertEquals("new_hashed_password", baseUser.getPassword());
            verify(userRepository, times(1)).save(baseUser);
        }

        @Test
        @DisplayName("Debería lanzar RuntimeException si la contraseña actual no coincide")
        void changePassword_WrongCurrentPassword_ShouldThrowException() {
            // Arrange
            when(userRepository.findByEmail(email)).thenReturn(Optional.of(baseUser));
            when(passwordEncoder.matches(request.getCurrentPassword(), baseUser.getPassword())).thenReturn(false);

            // Act & Assert
            RuntimeException ex = assertThrows(RuntimeException.class, () ->
                    userSettingsUseCase.changePassword(email, request)
            );
            assertEquals("La contraseña actual es incorrecta.", ex.getMessage());
        }

        @Test
        @DisplayName("Debería lanzar RuntimeException si la confirmación de la contraseña no coincide")
        void changePassword_PasswordsDoNotMatch_ShouldThrowException() {
            // Arrange
            request.setConfirmPassword("UnmatchedPass123!");
            when(userRepository.findByEmail(email)).thenReturn(Optional.of(baseUser));
            when(passwordEncoder.matches(request.getCurrentPassword(), baseUser.getPassword())).thenReturn(true);

            // Act & Assert
            RuntimeException ex = assertThrows(RuntimeException.class, () ->
                    userSettingsUseCase.changePassword(email, request)
            );
            assertEquals("Las contraseñas no coinciden.", ex.getMessage());
        }

        @Test
        @DisplayName("Debería lanzar RuntimeException si la contraseña nueva es idéntica a la actual")
        void changePassword_NewPasswordSameAsOld_ShouldThrowException() {
            // Arrange
            when(userRepository.findByEmail(email)).thenReturn(Optional.of(baseUser));
            when(passwordEncoder.matches(request.getCurrentPassword(), baseUser.getPassword())).thenReturn(true);
            when(passwordEncoder.matches(request.getNewPassword(), baseUser.getPassword())).thenReturn(true);

            // Act & Assert
            RuntimeException ex = assertThrows(RuntimeException.class, () ->
                    userSettingsUseCase.changePassword(email, request)
            );
            assertEquals("La nueva contraseña debe ser distinta a la actual.", ex.getMessage());
        }
    }

    @Nested
    @DisplayName("Pruebas para deleteAccount")
    class DeleteAccountTests {

        private DeleteAccountRequest request;

        @BeforeEach
        void setUp() {
            request = new DeleteAccountRequest();
            request.setPassword("correct_password");
        }

        @Test
        @DisplayName("Debería eliminar cuenta de usuario y su multimedia si la clave es válida")
        void deleteAccount_ValidPassword_ShouldDeleteUserAndImage() throws IOException {
            // Arrange
            when(userRepository.findByEmail(email)).thenReturn(Optional.of(baseUser));
            when(passwordEncoder.matches(request.getPassword(), baseUser.getPassword())).thenReturn(true);

            // Act
            userSettingsUseCase.deleteAccount(email, request);

            // Assert
            verify(cloudinaryFileCloudAdapter, times(1)).deleteImage("http://cloudinary.com/old_image.png");
            verify(userRepository, times(1)).delete(baseUser);
        }

        @Test
        @DisplayName("Debería lanzar RuntimeException si la contraseña de confirmación es incorrecta")
        void deleteAccount_IncorrectPassword_ShouldThrowRuntimeException() throws IOException {
            // Arrange
            when(userRepository.findByEmail(email)).thenReturn(Optional.of(baseUser));
            when(passwordEncoder.matches(request.getPassword(), baseUser.getPassword())).thenReturn(false);

            // Act & Assert
            RuntimeException ex = assertThrows(RuntimeException.class, () ->
                    userSettingsUseCase.deleteAccount(email, request)
            );
            assertEquals("La contraseña ingresada es incorrecta.", ex.getMessage());

            verify(cloudinaryFileCloudAdapter, never()).deleteImage(anyString());
            verify(userRepository, never()).delete(any(User.class));
        }
    }

    @Nested
    @DisplayName("Pruebas para deleteUserImage")
    class DeleteUserImageTests {

        @Test
        @DisplayName("Debería borrar imagen física del Storage y setear imagen de perfil en null")
        void deleteUserImage_HasImage_ShouldDeleteFromCloudAndDatabase() throws IOException {
            // Arrange
            when(userRepository.findByEmail(email)).thenReturn(Optional.of(baseUser));

            // Act
            userSettingsUseCase.deleteUserImage(email);

            // Assert
            assertNull(baseUser.getImage());
            verify(cloudinaryFileCloudAdapter, times(1)).deleteImage("http://cloudinary.com/old_image.png");
            verify(userRepository, times(1)).save(baseUser);
        }

        @Test
        @DisplayName("No debería realizar operaciones si el usuario no tiene ninguna imagen")
        void deleteUserImage_NoImageRegistered_ShouldDoNothing() throws IOException {
            // Arrange
            baseUser.setImage(null);
            when(userRepository.findByEmail(email)).thenReturn(Optional.of(baseUser));

            // Act
            userSettingsUseCase.deleteUserImage(email);

            // Assert
            verify(cloudinaryFileCloudAdapter, never()).deleteImage(anyString());
            verify(userRepository, never()).save(any(User.class));
        }
    }
}