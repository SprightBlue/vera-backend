package com.unlam.verabackend.application.usecase;

import com.unlam.verabackend.domain.model.UserLocation;
import com.unlam.verabackend.domain.port.out.GeoLocationProvider;
import com.unlam.verabackend.domain.port.out.UserLocationRepository;
import com.unlam.verabackend.infrastructure.entity.TrustContact;
import com.unlam.verabackend.infrastructure.repository.TrustContactRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UpdateLocationUseCaseImplTest {

    @Mock
    private UserLocationRepository repository;

    @Mock
    private GeoLocationProvider geoService;

    @Mock
    private TrustContactRepository trustContactRepository;

    @InjectMocks
    private UpdateLocationUseCaseImpl updateLocationUseCase;

    @Test
    void execute_ShouldUpdateLocation_WhenContactExists() {
        String email = "test@test.com";
        BigDecimal lat = new BigDecimal("10.0");
        BigDecimal lon = new BigDecimal("20.0");
        TrustContact contact = new TrustContact();
        contact.setId(1L);

        when(trustContactRepository.findByProtectedUser_Email(email)).thenReturn(Optional.of(contact));
        when(repository.findByTrustContactId(1L)).thenReturn(Optional.of(new UserLocation()));
        when(geoService.getAddressFromCoords(lat, lon)).thenReturn("Calle Falsa 123");
        when(repository.save(any(UserLocation.class), eq(1L))).thenAnswer(i -> i.getArguments()[0]);

        UserLocation result = updateLocationUseCase.execute(email, lat, lon);

        assertNotNull(result);
        assertTrue(result.isConnected());
        assertEquals("Calle Falsa 123", result.getLocationText());
        verify(repository).save(any(UserLocation.class), eq(1L));
    }

    @Test
    void execute_ShouldThrowException_WhenContactNotFound() {
        String email = "none@test.com";
        when(trustContactRepository.findByProtectedUser_Email(email)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () ->
                updateLocationUseCase.execute(email, BigDecimal.ZERO, BigDecimal.ZERO)
        );

        verify(repository, never()).save(any(), anyLong());
    }

    @Test
    void execute_ShouldCreateNewLocation_WhenNoneExists() {
        String email = "test@test.com";
        TrustContact contact = new TrustContact();
        contact.setId(1L);
        when(trustContactRepository.findByProtectedUser_Email(email)).thenReturn(Optional.of(contact));
        when(repository.findByTrustContactId(1L)).thenReturn(Optional.empty());
        when(geoService.getAddressFromCoords(any(), any())).thenReturn("Direccion");

        updateLocationUseCase.execute(email, BigDecimal.ONE, BigDecimal.ONE);

        verify(repository).save(argThat(loc -> loc.getTrustContact() != null), eq(1L));
    }
}