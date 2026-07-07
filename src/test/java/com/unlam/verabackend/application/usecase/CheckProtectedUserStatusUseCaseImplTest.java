package com.unlam.verabackend.application.usecase;

import com.unlam.verabackend.domain.exception.ResourceNotFoundException;
import com.unlam.verabackend.infrastructure.repository.TrustContactRepository;
import com.unlam.verabackend.infrastructure.entity.TrustContact;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CheckProtectedUserStatusUseCaseImplTest {

    @Mock
    private TrustContactRepository trustContactRepository;

    @InjectMocks
    private CheckProtectedUserStatusUseCaseImpl checkProtectedUserStatusUseCase;

    @Test
    @DisplayName("Debe retornar true cuando el usuario es un protegido activo")
    void execute_WhenUserIsProtected_ShouldReturnTrue() {
        String email = "mia@gmail.com";
        TrustContact mockTrustContact = mock(TrustContact.class);

        when(trustContactRepository.findByProtectedUser_Email(email))
                .thenReturn(Optional.of(mockTrustContact));

        boolean result = checkProtectedUserStatusUseCase.execute(email);

        assertTrue(result);
        verify(trustContactRepository, times(1)).findByProtectedUser_Email(email);
    }

    @Test
    @DisplayName("Debe lanzar ResourceNotFoundException cuando el usuario no es protegido")
    void execute_WhenUserIsNotProtected_ShouldThrowResourceNotFoundException() {
        String email = "no_protegido@gmail.com";
        when(trustContactRepository.findByProtectedUser_Email(email)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> checkProtectedUserStatusUseCase.execute(email));

        assertEquals("El usuario con email no_protegido@gmail.com no está registrado como protegido.", exception.getMessage());
        verify(trustContactRepository, times(1)).findByProtectedUser_Email(email);
    }
}