package com.unlam.verabackend.application.usecase;

import com.unlam.verabackend.domain.exception.ResourceNotFoundException;
import com.unlam.verabackend.domain.model.Role;
import com.unlam.verabackend.domain.model.UserLocation;
import com.unlam.verabackend.domain.port.out.GeocodingProvider;
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

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Pruebas Unitarias para UpdateUserLocationUseCaseImpl")
class UpdateUserLocationUseCaseImplTest {

    @Mock
    private UserLocationRepository locationRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private TrustContactRepository trustContactRepository;
    @Mock
    private GeocodingProvider geocodingProvider;

    @InjectMocks
    private UpdateUserLocationUseCaseImpl updateUserLocationUseCase;

    private String protectedEmail;
    private BigDecimal defaultLat;
    private BigDecimal defaultLng;
    private User validProtectedUser;

    @BeforeEach
    void setUp() {
        protectedEmail = "protegido@unlam.edu.ar";

        defaultLat = new BigDecimal("-34.6685");
        defaultLng = new BigDecimal("-58.5633");

        validProtectedUser = new User();
        validProtectedUser.setEmail(protectedEmail);
        validProtectedUser.setRole(Role.PROTECTED);
    }

    @Test
    @DisplayName("Debería crear una nueva ubicación exitosamente cuando es la primera transmisión y se pasa texto explícito")
    void execute_FirstTime_WithIncomingText_ShouldCreateSuccessfully() {
        // Arrange
        String incomingText = "Casa de la abuela";
        TrustContact mockContact = new TrustContact();

        when(userRepository.findByEmail(protectedEmail)).thenReturn(Optional.of(validProtectedUser));
        when(locationRepository.findByProtectedUserEmail(protectedEmail)).thenReturn(Optional.empty());
        when(trustContactRepository.findByProtectedUser_Email(protectedEmail)).thenReturn(Optional.of(mockContact));
        when(locationRepository.save(any(UserLocation.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        UserLocation result = updateUserLocationUseCase.execute(protectedEmail, defaultLat, defaultLng, incomingText);

        // Assert
        assertNotNull(result);
        assertEquals(incomingText, result.getLocationText());
        assertEquals(defaultLat, result.getLatitude());
        assertEquals(defaultLng, result.getLongitude());
        assertTrue(result.isConnected());
        assertEquals(mockContact, result.getTrustContact());

        verify(geocodingProvider, never()).getAddressFromCoordinates(any(), any());
        verify(locationRepository, times(1)).save(any(UserLocation.class));
    }

    @Test
    @DisplayName("Debería llamar al GeocodingProvider por primera vez si no se provee texto explícito")
    void execute_FirstTime_WithoutIncomingText_ShouldTriggerGeocoding() {
        // Arrange
        String mockResolvedAddress = "Calle Falsa 123, San Justo";
        TrustContact mockContact = new TrustContact();

        when(userRepository.findByEmail(protectedEmail)).thenReturn(Optional.of(validProtectedUser));
        when(locationRepository.findByProtectedUserEmail(protectedEmail)).thenReturn(Optional.empty());
        when(trustContactRepository.findByProtectedUser_Email(protectedEmail)).thenReturn(Optional.of(mockContact));
        when(geocodingProvider.getAddressFromCoordinates(defaultLat, defaultLng)).thenReturn(mockResolvedAddress);
        when(locationRepository.save(any(UserLocation.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        UserLocation result = updateUserLocationUseCase.execute(protectedEmail, defaultLat, defaultLng, null);

        // Assert
        assertNotNull(result);
        assertEquals(mockResolvedAddress, result.getLocationText());
        verify(geocodingProvider, times(1)).getAddressFromCoordinates(defaultLat, defaultLng);
    }

    @Test
    @DisplayName("Debería reutilizar el texto anterior de ubicación si el usuario se movió menos de 30 metros")
    void execute_ExistingLocation_InsideThreshold_ShouldReusePreviousText() {
        // Arrange
        String previousText = "Estación San Justo";
        UserLocation existingLocation = UserLocation.builder()
                .latitude(defaultLat)
                .longitude(defaultLng)
                .locationText(previousText)
                .build();

        BigDecimal subtleNewLat = defaultLat.add(new BigDecimal("0.00001"));
        BigDecimal subtleNewLng = defaultLng.add(new BigDecimal("0.00001"));

        when(userRepository.findByEmail(protectedEmail)).thenReturn(Optional.of(validProtectedUser));
        when(locationRepository.findByProtectedUserEmail(protectedEmail)).thenReturn(Optional.of(existingLocation));
        when(locationRepository.save(any(UserLocation.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        UserLocation result = updateUserLocationUseCase.execute(protectedEmail, subtleNewLat, subtleNewLng, "");

        // Assert
        assertNotNull(result);
        assertEquals(previousText, result.getLocationText());
        assertEquals(subtleNewLat, result.getLatitude());
        assertEquals(subtleNewLng, result.getLongitude());
        verify(geocodingProvider, never()).getAddressFromCoordinates(any(), any());
    }

    @Test
    @DisplayName("Debería retornar un texto por defecto si no se movió, no hay texto entrante y el previo era nulo")
    void execute_ExistingLocation_InsideThreshold_PreviousTextNull_ShouldReturnDefaultFallback() {
        // Arrange
        UserLocation existingLocation = UserLocation.builder()
                .latitude(defaultLat)
                .longitude(defaultLng)
                .locationText(null)
                .build();

        when(userRepository.findByEmail(protectedEmail)).thenReturn(Optional.of(validProtectedUser));
        when(locationRepository.findByProtectedUserEmail(protectedEmail)).thenReturn(Optional.of(existingLocation));
        when(locationRepository.save(any(UserLocation.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        UserLocation result = updateUserLocationUseCase.execute(protectedEmail, defaultLat, defaultLng, null);

        // Assert
        assertEquals("Ubicación en tiempo real", result.getLocationText());
    }

    @Test
    @DisplayName("Debería disparar geocodificación inversa si el usuario se movió significativamente (> 30 metros)")
    void execute_ExistingLocation_OutsideThreshold_ShouldCallGeocoding() {
        // Arrange
        UserLocation existingLocation = UserLocation.builder()
                .latitude(defaultLat)
                .longitude(defaultLng)
                .locationText("Ubicación Antigua")
                .build();

        BigDecimal distantNewLat = new BigDecimal("-34.7500");
        BigDecimal distantNewLng = new BigDecimal("-58.6500");
        String distantAddress = "Nueva Dirección Remota";

        when(userRepository.findByEmail(protectedEmail)).thenReturn(Optional.of(validProtectedUser));
        when(locationRepository.findByProtectedUserEmail(protectedEmail)).thenReturn(Optional.of(existingLocation));
        when(geocodingProvider.getAddressFromCoordinates(distantNewLat, distantNewLng)).thenReturn(distantAddress);
        when(locationRepository.save(any(UserLocation.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        UserLocation result = updateUserLocationUseCase.execute(protectedEmail, distantNewLat, distantNewLng, null);

        // Assert
        assertNotNull(result);
        assertEquals(distantAddress, result.getLocationText());
        verify(geocodingProvider, times(1)).getAddressFromCoordinates(distantNewLat, distantNewLng);
    }

    @Test
    @DisplayName("Debería lanzar ResourceNotFoundException si el email emisor no está registrado en la base de datos")
    void execute_UserNotFound_ShouldThrowResourceNotFoundException() {
        // Arrange
        when(userRepository.findByEmail(protectedEmail)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () ->
                updateUserLocationUseCase.execute(protectedEmail, defaultLat, defaultLng, null)
        );
        verify(locationRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debería lanzar SecurityException si el usuario existe pero no posee el rol PROTECTED")
    void execute_InvalidRole_ShouldThrowSecurityException() {
        // Arrange
        User badRoleUser = new User();
        badRoleUser.setEmail(protectedEmail);
        badRoleUser.setRole(Role.CARER);

        when(userRepository.findByEmail(protectedEmail)).thenReturn(Optional.of(badRoleUser));

        // Act & Assert
        SecurityException exception = assertThrows(SecurityException.class, () ->
                updateUserLocationUseCase.execute(protectedEmail, defaultLat, defaultLng, null)
        );
        assertEquals("Acceso denegado: Solo los usuarios protegidos pueden emitir su ubicación.", exception.getMessage());
        verify(locationRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debería lanzar ResourceNotFoundException si es la primera vez que transmite pero no posee ningún contacto de confianza en el sistema")
    void execute_FirstTime_NoTrustContactAssigned_ShouldThrowResourceNotFoundException() {
        // Arrange
        when(userRepository.findByEmail(protectedEmail)).thenReturn(Optional.of(validProtectedUser));
        when(locationRepository.findByProtectedUserEmail(protectedEmail)).thenReturn(Optional.empty());
        when(trustContactRepository.findByProtectedUser_Email(protectedEmail)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () ->
                updateUserLocationUseCase.execute(protectedEmail, defaultLat, defaultLng, null)
        );
        verify(locationRepository, never()).save(any());
    }
}