package com.unlam.verabackend.application.usecase;

import com.unlam.verabackend.domain.exception.ResourceNotFoundException;
import com.unlam.verabackend.domain.model.Role;
import com.unlam.verabackend.domain.model.UserLocation;
import com.unlam.verabackend.domain.port.out.UserLocationRepository;
import com.unlam.verabackend.infrastructure.entity.TrustContact;
import com.unlam.verabackend.infrastructure.entity.User;
import com.unlam.verabackend.infrastructure.repository.TrustContactRepository;
import com.unlam.verabackend.infrastructure.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
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
@DisplayName("Pruebas Unitarias para GetUserLocationUseCaseImpl")
class GetUserLocationUseCaseImplTest {

    @Mock
    private UserLocationRepository locationRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private TrustContactRepository trustContactRepository;

    @InjectMocks
    private GetUserLocationUseCaseImpl getUserLocationUseCase;

    private String carerEmail;
    private Long trustContactId;
    private User validCarer;
    private User protectedUser;
    private TrustContact validContact;

    @BeforeEach
    void setUp() {
        carerEmail = "cuidador@unlam.edu.ar";
        trustContactId = 10L;

        validCarer = new User();
        validCarer.setEmail(carerEmail);
        validCarer.setRole(Role.CARER);

        protectedUser = new User();
        protectedUser.setEmail("protegido@unlam.edu.ar");
        protectedUser.setRole(Role.PROTECTED);

        validContact = new TrustContact();
        validContact.setId(trustContactId);
        validContact.setCarer(validCarer);
        validContact.setProtectedUser(protectedUser);
    }

    @Test
    @DisplayName("Debería retornar la ubicación del protegido exitosamente cuando el cuidador y el vínculo son válidos")
    void execute_ValidScenario_ShouldReturnUserLocation() {
        // Arrange
        UserLocation expectedLocation = new UserLocation();
        expectedLocation.setTrustContact(validContact);
        expectedLocation.setConnected(true);

        when(userRepository.findByEmail(carerEmail)).thenReturn(Optional.of(validCarer));
        when(trustContactRepository.findById(trustContactId)).thenReturn(Optional.of(validContact));
        when(locationRepository.findByProtectedUserEmail(protectedUser.getEmail())).thenReturn(Optional.of(expectedLocation));

        // Act
        UserLocation result = getUserLocationUseCase.execute(trustContactId, carerEmail);

        // Assert
        assertNotNull(result);
        assertTrue(result.isConnected());
        assertEquals(validContact, result.getTrustContact());

        verify(userRepository, times(1)).findByEmail(carerEmail);
        verify(trustContactRepository, times(1)).findById(trustContactId);
        verify(locationRepository, times(1)).findByProtectedUserEmail(protectedUser.getEmail());
    }

    @Test
    @DisplayName("Debería lanzar ResourceNotFoundException si el cuidador solicitante no está registrado")
    void execute_CarerNotFound_ShouldThrowResourceNotFoundException() {
        // Arrange
        when(userRepository.findByEmail(carerEmail)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () ->
                getUserLocationUseCase.execute(trustContactId, carerEmail)
        );

        verify(trustContactRepository, never()).findById(anyLong());
        verify(locationRepository, never()).findByProtectedUserEmail(anyString());
    }

    @Test
    @DisplayName("Debería lanzar SecurityException si el usuario solicitante existe pero no tiene el rol CARER")
    void execute_InvalidRole_ShouldThrowSecurityException() {
        // Arrange
        User invalidRoleUser = new User();
        invalidRoleUser.setEmail(carerEmail);
        invalidRoleUser.setRole(Role.PROTECTED); // Un protegido intentando espiar a otro

        when(userRepository.findByEmail(carerEmail)).thenReturn(Optional.of(invalidRoleUser));

        // Act & Assert
        SecurityException exception = assertThrows(SecurityException.class, () ->
                getUserLocationUseCase.execute(trustContactId, carerEmail)
        );

        assertEquals("Acceso denegado: Solo los cuidadores autorizados pueden visualizar mapas en tiempo real.", exception.getMessage());
        verify(trustContactRepository, never()).findById(anyLong());
    }

    @Test
    @DisplayName("Debería lanzar ResourceNotFoundException si el vínculo ID especificado no existe")
    void execute_TrustContactNotFound_ShouldThrowResourceNotFoundException() {
        // Arrange
        when(userRepository.findByEmail(carerEmail)).thenReturn(Optional.of(validCarer));
        when(trustContactRepository.findById(trustContactId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () ->
                getUserLocationUseCase.execute(trustContactId, carerEmail)
        );

        verify(locationRepository, never()).findByProtectedUserEmail(anyString());
    }

    @Test
    @DisplayName("Debería lanzar SecurityException si el vínculo existe pero le pertenece a otro cuidador")
    void execute_CarerMismatch_ShouldThrowSecurityException() {
        // Arrange
        User anotherCarer = new User();
        anotherCarer.setEmail("otro_cuidador@unlam.edu.ar");

        TrustContact maliciousContact = new TrustContact();
        maliciousContact.setId(trustContactId);
        maliciousContact.setCarer(anotherCarer); // No machea con el que consulta

        when(userRepository.findByEmail(carerEmail)).thenReturn(Optional.of(validCarer));
        when(trustContactRepository.findById(trustContactId)).thenReturn(Optional.of(maliciousContact));

        // Act & Assert
        SecurityException exception = assertThrows(SecurityException.class, () ->
                getUserLocationUseCase.execute(trustContactId, carerEmail)
        );

        assertEquals("Acceso denegado: No eres el cuidador asignado a este protegido.", exception.getMessage());
        verify(locationRepository, never()).findByProtectedUserEmail(anyString());
    }

    @Test
    @DisplayName("Debería lanzar ResourceNotFoundException si el vínculo es correcto pero el protegido nunca emitió coordenadas")
    void execute_LocationRecordsEmpty_ShouldThrowResourceNotFoundException() {
        // Arrange
        when(userRepository.findByEmail(carerEmail)).thenReturn(Optional.of(validCarer));
        when(trustContactRepository.findById(trustContactId)).thenReturn(Optional.of(validContact));
        when(locationRepository.findByProtectedUserEmail(protectedUser.getEmail())).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () ->
                getUserLocationUseCase.execute(trustContactId, carerEmail)
        );

        assertEquals("No hay registros geográficos disponibles para esta relación de confianza.", exception.getMessage());
    }
}