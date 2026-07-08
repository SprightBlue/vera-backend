//package com.unlam.verabackend.application.usecase;
//
//import com.unlam.verabackend.domain.exception.ResourceNotFoundException;
//import com.unlam.verabackend.domain.model.UserLocation;
//import com.unlam.verabackend.domain.port.out.UserLocationRepository;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//
//import java.util.Optional;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.Mockito.*;
//
//@ExtendWith(MockitoExtension.class)
//class GetUserLocationUseCaseImplTest {
//
//    @Mock
//    private UserLocationRepository locationRepository;
//
//    @InjectMocks
//    private GetUserLocationUseCaseImpl getUserLocationUseCase;
//
//    @Test
//    @DisplayName("Debe retornar la ubicación cuando el cuidador es el asignado (Camino Feliz)")
//    void execute_WhenCarerIsValid_ShouldReturnUserLocation() {
//        Long trustContactId = 1L;
//        String carerEmail = "cuidador@gmail.com";
//
//        UserLocation locationSpy = mock(UserLocation.class, RETURNS_DEEP_STUBS);
//        when(locationRepository.findByTrustContactId(trustContactId)).thenReturn(Optional.of(locationSpy));
//        when(locationSpy.getTrustContact().getCarer().getEmail()).thenReturn(carerEmail);
//
//        UserLocation result = getUserLocationUseCase.execute(trustContactId, carerEmail);
//
//        assertNotNull(result);
//        assertEquals(locationSpy, result);
//        verify(locationRepository, times(1)).findByTrustContactId(trustContactId);
//    }
//
//    @Test
//    @DisplayName("Debe lanzar ResourceNotFoundException si la relación no existe")
//    void execute_WhenLocationNotFound_ShouldThrowResourceNotFoundException() {
//        Long trustContactId = 99L;
//        String carerEmail = "cuidador@gmail.com";
//        when(locationRepository.findByTrustContactId(trustContactId)).thenReturn(Optional.empty());
//
//        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () ->
//                getUserLocationUseCase.execute(trustContactId, carerEmail)
//        );
//
//        assertEquals("No hay registros de ubicación para esta relación.", exception.getMessage());
//        verify(locationRepository, times(1)).findByTrustContactId(trustContactId);
//    }
//
//    @Test
//    @DisplayName("Debe lanzar SecurityException si el email no pertenece al cuidador asignado")
//    void execute_WhenCarerIsInvalid_ShouldThrowSecurityException() {
//        Long trustContactId = 1L;
//        String fraudulentCarerEmail = "hacker@gmail.com";
//        String realCarerEmail = "cuidador_real@gmail.com";
//
//        UserLocation locationSpy = mock(UserLocation.class, RETURNS_DEEP_STUBS);
//        when(locationRepository.findByTrustContactId(trustContactId)).thenReturn(Optional.of(locationSpy));
//        when(locationSpy.getTrustContact().getCarer().getEmail()).thenReturn(realCarerEmail);
//
//        SecurityException exception = assertThrows(SecurityException.class, () ->
//                getUserLocationUseCase.execute(trustContactId, fraudulentCarerEmail)
//        );
//
//        assertEquals("Acceso denegado: No eres el cuidador asignado.", exception.getMessage());
//        verify(locationRepository, times(1)).findByTrustContactId(trustContactId);
//    }
//}