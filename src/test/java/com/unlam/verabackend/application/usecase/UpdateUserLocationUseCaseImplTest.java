//package com.unlam.verabackend.application.usecase;
//
//import com.unlam.verabackend.domain.exception.ResourceNotFoundException;
//import com.unlam.verabackend.domain.model.UserLocation;
//import com.unlam.verabackend.domain.port.out.GeocodingProvider;
//import com.unlam.verabackend.domain.port.out.UserLocationRepository;
//import com.unlam.verabackend.infrastructure.entity.TrustContact;
//import com.unlam.verabackend.infrastructure.repository.TrustContactRepository;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//
//import java.math.BigDecimal;
//import java.util.Optional;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.Mockito.*;
//
//@ExtendWith(MockitoExtension.class)
//class UpdateUserLocationUseCaseImplTest {
//
//    @Mock
//    private UserLocationRepository locationRepository;
//    @Mock
//    private TrustContactRepository trustContactRepository;
//    @Mock
//    private GeocodingProvider geocodingProvider;
//
//    @InjectMocks
//    private UpdateUserLocationUseCaseImpl updateUserLocationUseCase;
//
//    @Test
//    @DisplayName("Debe crear una nueva ubicación consultando OSM si es la primera vez que se registra")
//    void execute_WhenNoPreviousLocation_ShouldConsultOSMAndSave() {
//        String email = "test@unlam.com";
//        BigDecimal lat = new BigDecimal("-34.670000");
//        BigDecimal lng = new BigDecimal("-58.560000");
//
//        when(locationRepository.findByProtectedUserEmail(email)).thenReturn(Optional.empty());
//        when(trustContactRepository.findByProtectedUser_Email(email)).thenReturn(Optional.of(new TrustContact()));
//        when(geocodingProvider.getAddressFromCoordinates(lat, lng)).thenReturn("San Justo, Buenos Aires");
//        when(locationRepository.save(any(UserLocation.class))).thenAnswer(invocation -> invocation.getArgument(0));
//
//        UserLocation result = updateUserLocationUseCase.execute(email, lat, lng, null);
//
//        assertNotNull(result);
//        assertEquals("San Justo, Buenos Aires", result.getLocationText());
//        assertTrue(result.isConnected());
//        verify(geocodingProvider, times(1)).getAddressFromCoordinates(lat, lng);
//    }
//
//    @Test
//    @DisplayName("Debe lanzar ResourceNotFoundException si no hay registro previo ni relación de confianza activa")
//    void execute_WhenNoTrustContactFound_ShouldThrowException() {
//        String email = "hacker@unlam.com";
//        BigDecimal lat = new BigDecimal("-34.670000");
//        BigDecimal lng = new BigDecimal("-58.560000");
//
//        when(locationRepository.findByProtectedUserEmail(email)).thenReturn(Optional.empty());
//        when(trustContactRepository.findByProtectedUser_Email(email)).thenReturn(Optional.empty());
//
//        assertThrows(ResourceNotFoundException.class, () ->
//                updateUserLocationUseCase.execute(email, lat, lng, "")
//        );
//        verify(locationRepository, never()).save(any());
//    }
//
//    @Test
//    @DisplayName("Debe actualizar usando el texto provisto directamente sin consultar OSM")
//    void execute_WhenLocationTextIsProvided_ShouldUseItDirectly() {
//        String email = "test@unlam.com";
//        BigDecimal lat = new BigDecimal("-34.670000");
//        BigDecimal lng = new BigDecimal("-58.560000");
//        UserLocation existingLocation = UserLocation.builder().latitude(lat).longitude(lng).build();
//
//        when(locationRepository.findByProtectedUserEmail(email)).thenReturn(Optional.of(existingLocation));
//        when(locationRepository.save(any(UserLocation.class))).thenAnswer(invocation -> invocation.getArgument(0));
//
//        UserLocation result = updateUserLocationUseCase.execute(email, lat, lng, "Ubicación Manual");
//
//        assertEquals("Ubicación Manual", result.getLocationText());
//        verify(geocodingProvider, never()).getAddressFromCoordinates(any(), any());
//    }
//
//    @Test
//    @DisplayName("Debe consultar OSM si el usuario se movió significativamente (más de 30 metros)")
//    void execute_WhenUserMovedSignificantly_ShouldQueryOSM() {
//        String email = "test@unlam.com";
//
//        BigDecimal oldLat = new BigDecimal("-34.603738");
//        BigDecimal oldLng = new BigDecimal("-58.381570");
//
//        BigDecimal newLat = new BigDecimal("-34.608300");
//        BigDecimal newLng = new BigDecimal("-58.373100");
//
//        UserLocation existingLocation = UserLocation.builder()
//                .latitude(oldLat)
//                .longitude(oldLng)
//                .locationText("Cerca del Obelisco")
//                .build();
//
//        when(locationRepository.findByProtectedUserEmail(email)).thenReturn(Optional.of(existingLocation));
//        when(geocodingProvider.getAddressFromCoordinates(newLat, newLng)).thenReturn("Plaza de Mayo");
//        when(locationRepository.save(any(UserLocation.class))).thenAnswer(invocation -> invocation.getArgument(0));
//
//        UserLocation result = updateUserLocationUseCase.execute(email, newLat, newLng, null);
//
//        assertEquals("Plaza de Mayo", result.getLocationText());
//        verify(geocodingProvider, times(1)).getAddressFromCoordinates(newLat, newLng);
//    }
//
//    @Test
//    @DisplayName("Debe omitir OSM y mantener el texto si el movimiento es insignificante")
//    void execute_WhenUserMovementIsMinimal_ShouldReuseAddress() {
//        String email = "test@unlam.com";
//
//        BigDecimal oldLat = new BigDecimal("-34.670000");
//        BigDecimal oldLng = new BigDecimal("-58.560000");
//        BigDecimal newLat = new BigDecimal("-34.670001");
//        BigDecimal newLng = new BigDecimal("-58.560001");
//
//        UserLocation existingLocation = UserLocation.builder()
//                .latitude(oldLat)
//                .longitude(oldLng)
//                .locationText("Dirección Anterior Guardada")
//                .build();
//
//        when(locationRepository.findByProtectedUserEmail(email)).thenReturn(Optional.of(existingLocation));
//        when(locationRepository.save(any(UserLocation.class))).thenAnswer(invocation -> invocation.getArgument(0));
//
//        UserLocation result = updateUserLocationUseCase.execute(email, newLat, newLng, null);
//
//        assertEquals("Dirección Anterior Guardada", result.getLocationText());
//        verify(geocodingProvider, never()).getAddressFromCoordinates(any(), any());
//    }
//
//    @Test
//    @DisplayName("Debe setear texto por defecto si el movimiento es mínimo pero no poseía texto previo")
//    void execute_WhenMovementIsMinimalButNoTextExists_ShouldSetDefaultText() {
//        String email = "test@unlam.com";
//        BigDecimal oldLat = new BigDecimal("-34.670000");
//        BigDecimal oldLng = new BigDecimal("-58.560000");
//
//        UserLocation existingLocation = UserLocation.builder()
//                .latitude(oldLat)
//                .longitude(oldLng)
//                .locationText(null)
//                .build();
//
//        when(locationRepository.findByProtectedUserEmail(email)).thenReturn(Optional.of(existingLocation));
//        when(locationRepository.save(any(UserLocation.class))).thenAnswer(invocation -> invocation.getArgument(0));
//
//        UserLocation result = updateUserLocationUseCase.execute(email, oldLat, oldLng, "");
//
//        assertEquals("Ubicación en tiempo real", result.getLocationText());
//    }
//}