package com.unlam.verabackend.application.usecase;

import com.unlam.verabackend.domain.exception.ResourceNotFoundException;
import com.unlam.verabackend.domain.model.ScenarioOption;
import com.unlam.verabackend.domain.model.TrainingScenario;
import com.unlam.verabackend.domain.model.TrainingSession;
import com.unlam.verabackend.domain.port.out.TrainingRepository;
import com.unlam.verabackend.infrastructure.entity.TrustContact;
import com.unlam.verabackend.infrastructure.entity.User;
import com.unlam.verabackend.infrastructure.repository.TrustContactRepository;
import com.unlam.verabackend.infrastructure.repository.UserRepository;
import com.unlam.verabackend.presentation.dto.TrainingProgressResponse;
import com.unlam.verabackend.presentation.dto.TrainingStatsResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ManageTrainingUseCaseImplTest {

    @Mock
    private TrainingRepository trainingRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private TrustContactRepository trustContactRepository;

    @InjectMocks
    private ManageTrainingUseCaseImpl useCase;

    private User usuarioProtegido;
    private User cuidador;
    private TrustContact relacionConfianza;
    private UUID escenarioId;
    private UUID opcionId;
    private TrainingScenario escenarioMock;

    @BeforeEach
    void setUp() {
        usuarioProtegido = new User();
        ReflectionTestUtils.setField(usuarioProtegido, "id", 1L);
        usuarioProtegido.setEmail("abuelo@gmail.com");

        cuidador = new User();
        ReflectionTestUtils.setField(cuidador, "id", 2L);
        cuidador.setEmail("hijo@gmail.com");

        relacionConfianza = TrustContact.builder()
                .id(100L)
                .protectedUser(usuarioProtegido)
                .carer(cuidador)
                .build();

        escenarioId = UUID.randomUUID();
        opcionId = UUID.randomUUID();

        ScenarioOption opcionMock = mock(ScenarioOption.class);
        lenient().when(opcionMock.getId()).thenReturn(opcionId);
        lenient().when(opcionMock.isCorrect()).thenReturn(true);

        escenarioMock = mock(TrainingScenario.class);
        lenient().when(escenarioMock.getId()).thenReturn(escenarioId);
        lenient().when(escenarioMock.getOptions()).thenReturn(List.of(opcionMock));
    }


    @Test
    void deberiaObtenerEscenariosDisponibles() {
        when(trainingRepository.findActiveScenarios()).thenReturn(List.of(escenarioMock));
        List<TrainingScenario> resultado = useCase.getAvailableScenarios("random@gmail.com");
        assertFalse(resultado.isEmpty());
    }

    @Test
    void deberiaObtenerEscenarioPorId() {
        when(trainingRepository.findScenarioById(escenarioId)).thenReturn(Optional.of(escenarioMock));
        TrainingScenario resultado = useCase.getScenarioById(escenarioId);
        assertEquals(escenarioId, resultado.getId());
    }

    @Test
    void deberiaLanzarExcepcionSiNoEncuentraEscenario() {
        when(trainingRepository.findScenarioById(escenarioId)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> useCase.getScenarioById(escenarioId));
    }

    @Test
    void deberiaCompletarSesionPendienteAlEnviarRespuesta() {
        when(userRepository.findByEmail(usuarioProtegido.getEmail())).thenReturn(Optional.of(usuarioProtegido));
        when(trainingRepository.findScenarioById(escenarioId)).thenReturn(Optional.of(escenarioMock));

        TrainingSession sesionPendiente = mock(TrainingSession.class);
        when(sesionPendiente.getId()).thenReturn(UUID.randomUUID());
        when(trainingRepository.findPendingSession(usuarioProtegido.getId(), escenarioId)).thenReturn(Optional.of(sesionPendiente));
        
        TrainingSession sesionCompletada = mock(TrainingSession.class);
        when(trainingRepository.completeSession(any(), any(), anyBoolean())).thenReturn(sesionCompletada);

        TrainingSession resultado = useCase.submitAnswer(usuarioProtegido.getEmail(), escenarioId, opcionId);

        assertNotNull(resultado);
        verify(trainingRepository, times(1)).completeSession(any(), eq(opcionId), eq(true));
        verify(trainingRepository, never()).saveSession(any());
    }

    @Test
    void deberiaCrearNuevaSesionAlEnviarRespuestaSiNoHayPendiente() {
        when(userRepository.findByEmail(usuarioProtegido.getEmail())).thenReturn(Optional.of(usuarioProtegido));
        when(trainingRepository.findScenarioById(escenarioId)).thenReturn(Optional.of(escenarioMock));
        when(trainingRepository.findPendingSession(usuarioProtegido.getId(), escenarioId)).thenReturn(Optional.empty());

        useCase.submitAnswer(usuarioProtegido.getEmail(), escenarioId, opcionId);

        verify(trainingRepository, times(1)).saveSession(any(TrainingSession.class));
    }

    @Test
    void deberiaLanzarExcepcionAlEnviarRespuestaSiOpcionNoExiste() {
        when(userRepository.findByEmail(usuarioProtegido.getEmail())).thenReturn(Optional.of(usuarioProtegido));
        when(trainingRepository.findScenarioById(escenarioId)).thenReturn(Optional.of(escenarioMock));
        
        UUID opcionInvalidaId = UUID.randomUUID();

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            useCase.submitAnswer(usuarioProtegido.getEmail(), escenarioId, opcionInvalidaId);
        });
        assertTrue(exception.getMessage().contains("Opción no encontrada"));
    }


    @Test
    void deberiaAsignarEscenarioConExito() {
        when(trustContactRepository.findById(100L)).thenReturn(Optional.of(relacionConfianza));
        when(trainingRepository.findScenarioById(escenarioId)).thenReturn(Optional.of(escenarioMock));

        useCase.assignScenario(cuidador.getEmail(), 100L, escenarioId);

        verify(trainingRepository, times(1)).saveSession(any(TrainingSession.class));
    }

    @Test
    void deberiaLanzarExcepcionDeAccesoSiCuidadorNoEsElDuenioDeLaRelacion() {
        when(trustContactRepository.findById(100L)).thenReturn(Optional.of(relacionConfianza));

        AccessDeniedException exception = assertThrows(AccessDeniedException.class, () -> {
            useCase.assignScenario("intruso@gmail.com", 100L, escenarioId);
        });
        assertEquals("No tenés permisos para acceder a estos datos.", exception.getMessage());
    }


    @Test
    void deberiaObtenerProgresoConEstadisticasCalculadasCorrectamente() {
        when(trustContactRepository.findById(100L)).thenReturn(Optional.of(relacionConfianza));

        LocalDateTime hoy = LocalDateTime.of(2026, 7, 10, 15, 0);

        TrainingSession sesionCorrecta = mock(TrainingSession.class, Answers.RETURNS_DEEP_STUBS);
        lenient().when(sesionCorrecta.getId()).thenReturn(UUID.randomUUID());
        lenient().when(sesionCorrecta.isCorrect()).thenReturn(true);
        lenient().when(sesionCorrecta.getCompletedAt()).thenReturn(hoy);
        lenient().when(sesionCorrecta.getScenario().isScam()).thenReturn(true);
        lenient().when(sesionCorrecta.getScenario().getTitle()).thenReturn("Estafa Whatsapp");
        lenient().when(sesionCorrecta.getScenario().getScenarioType().name()).thenReturn("PHISHING");

        TrainingSession sesionIncorrecta = mock(TrainingSession.class, Answers.RETURNS_DEEP_STUBS);
        lenient().when(sesionIncorrecta.getId()).thenReturn(UUID.randomUUID());
        lenient().when(sesionIncorrecta.isCorrect()).thenReturn(false); 
        lenient().when(sesionIncorrecta.getCompletedAt()).thenReturn(hoy); 
        lenient().when(sesionIncorrecta.getScenario().isScam()).thenReturn(true);
        lenient().when(sesionIncorrecta.getScenario().getTitle()).thenReturn("Cuento del Tio");
        lenient().when(sesionIncorrecta.getScenario().getScenarioType().name()).thenReturn("VISHING");

        when(trainingRepository.findCompletedSessionsByUserId(usuarioProtegido.getId()))
                .thenReturn(List.of(sesionCorrecta, sesionIncorrecta));

        TrainingProgressResponse progreso = useCase.getProgressForProtected(cuidador.getEmail(), 100L);

        assertEquals(2, progreso.stats().completed());
        assertEquals(1, progreso.stats().correct());
        assertEquals(1, progreso.stats().incorrect());
        assertEquals(50, progreso.stats().correctRate()); 

        assertEquals(1, progreso.dailyProgress().size());
        assertEquals("10/07", progreso.dailyProgress().get(0).date());
        assertEquals(50, progreso.dailyProgress().get(0).correctRate()); 
        assertEquals(2, progreso.dailyProgress().get(0).total()); 

        assertEquals(2, progreso.recentSessions().size());
        assertEquals("Estafa Whatsapp", progreso.recentSessions().get(0).scenarioTitle());
    }

    @Test
    void deberiaObtenerEstadisticasAisladasDelProtegido() {
        when(trustContactRepository.findById(100L)).thenReturn(Optional.of(relacionConfianza));
        when(trainingRepository.findCompletedSessionsByUserId(usuarioProtegido.getId())).thenReturn(List.of());

        TrainingStatsResponse stats = useCase.getStatsForProtected(cuidador.getEmail(), 100L);
        assertEquals(0, stats.completed());
        assertEquals(0, stats.correct());
        assertEquals(0, stats.incorrect());
        assertEquals(0, stats.correctRate());
    }

    @Test
    void deberiaObtenerSesionesPaginadasDelProtegido() {
        when(trustContactRepository.findById(100L)).thenReturn(Optional.of(relacionConfianza));
        
        Pageable pageable = PageRequest.of(0, 10);
        Page<TrainingSession> paginaMock = new PageImpl<>(List.of(mock(TrainingSession.class)));
        
        when(trainingRepository.findSessionsByUserId(usuarioProtegido.getId(), pageable)).thenReturn(paginaMock);

        Page<TrainingSession> resultado = useCase.getSessionsForProtected(cuidador.getEmail(), 100L, pageable);
        assertEquals(1, resultado.getContent().size());
    }
}