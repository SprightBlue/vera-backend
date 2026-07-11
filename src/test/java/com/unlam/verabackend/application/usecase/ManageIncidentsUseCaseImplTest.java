package com.unlam.verabackend.application.usecase;

import com.unlam.verabackend.application.service.IncidentStepBuilderService;
import com.unlam.verabackend.domain.exception.ResourceNotFoundException;
import com.unlam.verabackend.domain.model.*;
import com.unlam.verabackend.domain.port.out.IncidentRepository;
import com.unlam.verabackend.infrastructure.entity.TrustContact;
import com.unlam.verabackend.infrastructure.entity.User;
import com.unlam.verabackend.infrastructure.repository.TrustContactRepository;
import com.unlam.verabackend.infrastructure.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ManageIncidentsUseCaseImplTest {

    @Mock
    private IncidentRepository incidentRepository;
    @Mock
    private IncidentStepBuilderService stepBuilderService;
    @Mock
    private UserRepository userRepository;
    @Mock
    private TrustContactRepository trustContactRepository;

    @InjectMocks
    private ManageIncidentsUseCaseImpl useCase;

    private User victima;
    private User cuidador;
    private User intruso;
    private Incident incidenteMock;
    private UUID incidenteId;

    @BeforeEach
    void setUp() {
        victima = new User();
        ReflectionTestUtils.setField(victima, "id", 1L);
        victima.setEmail("abuelo@gmail.com");

        cuidador = new User();
        ReflectionTestUtils.setField(cuidador, "id", 2L);
        cuidador.setEmail("hijo@gmail.com");

        intruso = new User();
        ReflectionTestUtils.setField(intruso, "id", 3L);
        intruso.setEmail("desconocido@gmail.com");

        incidenteId = UUID.randomUUID();
        incidenteMock = Incident.builder()
                .id(incidenteId)
                .user(victima)
                .status(IncidentStatus.IN_PROGRESS)
                .steps(new ArrayList<>())
                .build();
    }

    @Test
    void deberiaCrearIncidenteConExito() {
        String email = victima.getEmail();
        IncidentActionType actionType = IncidentActionType.CLICKED_SUSPICIOUS_LINK;
        List<SharedDataType> sharedData = List.of();
        String descripcion = "Hice click en un mail del banco falso";

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(victima));
        
        List<IncidentStep> pasosGenerados = List.of(IncidentStep.builder().stepKey(IncidentStepKey.SCAN_DEVICE).build());
        when(stepBuilderService.buildSteps(actionType, sharedData)).thenReturn(pasosGenerados);
        
        when(incidentRepository.save(any(Incident.class))).thenReturn(incidenteMock);

        Incident resultado = useCase.createIncident(email, actionType, sharedData, descripcion);

        assertNotNull(resultado);
        verify(incidentRepository, times(1)).save(any(Incident.class));
    }

    @Test
    void deberiaPermitirVerDetalleAlDueñoDelIncidente() {
        when(incidentRepository.findById(incidenteId)).thenReturn(Optional.of(incidenteMock));

        Incident resultado = useCase.getIncidentDetail(incidenteId, victima.getEmail());

        assertEquals(incidenteId, resultado.getId());
    }

    @Test
    void deberiaPermitirVerDetalleAlCuidadorAutorizado() {
        when(incidentRepository.findById(incidenteId)).thenReturn(Optional.of(incidenteMock));
        
        when(userRepository.findByEmail(cuidador.getEmail())).thenReturn(Optional.of(cuidador));
        when(trustContactRepository.existsByCarerIdAndProtectedUser_Id(cuidador.getId(), victima.getId())).thenReturn(true);

        Incident resultado = useCase.getIncidentDetail(incidenteId, cuidador.getEmail());

        assertEquals(incidenteId, resultado.getId());
    }

    @Test
    void deberiaLanzarExcepcionSiUnIntrusoIntentaVerElIncidente() {
        when(incidentRepository.findById(incidenteId)).thenReturn(Optional.of(incidenteMock));
        
        when(userRepository.findByEmail(intruso.getEmail())).thenReturn(Optional.of(intruso));
        when(trustContactRepository.existsByCarerIdAndProtectedUser_Id(intruso.getId(), victima.getId())).thenReturn(false);

        AccessDeniedException exception = assertThrows(AccessDeniedException.class, () -> {
            useCase.getIncidentDetail(incidenteId, intruso.getEmail());
        });
        assertEquals("No tenés permisos para ver este incidente.", exception.getMessage());
    }

    @Test
    void deberiaCompletarPasoYNoFinalizarIncidenteSiFaltanPasos() {

        UUID paso1Id = UUID.randomUUID();
        UUID paso2Id = UUID.randomUUID();

        IncidentStep paso1 = IncidentStep.builder()
                .id(paso1Id)
                .stepKey(IncidentStepKey.SCAN_DEVICE)
                .completed(false)
                .build();
                
        IncidentStep paso2 = IncidentStep.builder()
                .id(paso2Id)
                .stepKey(IncidentStepKey.CHANGE_HOMEBANKING_PASSWORD)
                .completed(false)
                .build();

        incidenteMock.setSteps(List.of(paso1, paso2));

        when(incidentRepository.findById(incidenteId)).thenReturn(Optional.of(incidenteMock));

        useCase.completeStep(incidenteId, IncidentStepKey.SCAN_DEVICE, victima.getEmail());

        verify(incidentRepository, times(1)).completeStep(paso1Id);
        verify(incidentRepository, never()).markIncidentCompleted(any());
    } 

    @Test
    void deberiaLanzarExcepcionAlCompletarPasoSiNoEsDueno() {
        when(incidentRepository.findById(incidenteId)).thenReturn(Optional.of(incidenteMock));

        AccessDeniedException exception = assertThrows(AccessDeniedException.class, () -> {
            useCase.completeStep(incidenteId, IncidentStepKey.SCAN_DEVICE, cuidador.getEmail());
        });
        assertEquals("No tenés permisos para modificar este incidente.", exception.getMessage());
    }

    @Test
    void deberiaObtenerMisIncidentes() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Incident> pagina = new PageImpl<>(List.of(incidenteMock));

        when(userRepository.findByEmail(victima.getEmail())).thenReturn(Optional.of(victima));
        when(incidentRepository.findByUserId(victima.getId(), pageable)).thenReturn(pagina);

        Page<Incident> resultado = useCase.getMyIncidents(victima.getEmail(), pageable);

        assertEquals(1, resultado.getContent().size());
    }

    @Test
    void deberiaObtenerIncidentesDeContactoDeConfianza() {
        Long trustContactId = 55L;
        Pageable pageable = PageRequest.of(0, 10);
        Page<Incident> pagina = new PageImpl<>(List.of(incidenteMock));

        TrustContact relacionMock = TrustContact.builder()
                .carer(cuidador)
                .protectedUser(victima)
                .build();

        when(trustContactRepository.findById(trustContactId)).thenReturn(Optional.of(relacionMock));
        when(incidentRepository.findByUserId(victima.getId(), pageable)).thenReturn(pagina);

        Page<Incident> resultado = useCase.getIncidentsByTrustContact(trustContactId, cuidador.getEmail(), pageable);

        assertEquals(1, resultado.getContent().size());
    }

    @Test
    void deberiaLanzarExcepcionAlPedirIncidentesDeContactoSiNoCoincideEmail() {
        Long trustContactId = 55L;
        TrustContact relacionMock = TrustContact.builder()
                .carer(cuidador)
                .protectedUser(victima)
                .build();

        when(trustContactRepository.findById(trustContactId)).thenReturn(Optional.of(relacionMock));

        AccessDeniedException exception = assertThrows(AccessDeniedException.class, () -> {
            useCase.getIncidentsByTrustContact(trustContactId, intruso.getEmail(), PageRequest.of(0, 10));
        });
        
        assertEquals("No tenés permisos para ver estos incidentes.", exception.getMessage());
    }
}