package com.unlam.verabackend.application.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import com.unlam.verabackend.application.service.CloudinaryService;
import com.unlam.verabackend.application.service.NotificationService;
import com.unlam.verabackend.domain.model.InvitationStatus;
import com.unlam.verabackend.domain.model.SensitivityLevel;
import com.unlam.verabackend.infrastructure.entity.TrustContact;
import com.unlam.verabackend.infrastructure.entity.TrustInvitation;
import com.unlam.verabackend.infrastructure.entity.User;
import com.unlam.verabackend.infrastructure.repository.TrustContactRepository;
import com.unlam.verabackend.infrastructure.repository.TrustInvitationRepository;
import com.unlam.verabackend.infrastructure.repository.UserRepository;
import com.unlam.verabackend.presentation.dto.CarerResponse;
import com.unlam.verabackend.presentation.dto.GenerateInvitationRequest;
import com.unlam.verabackend.presentation.dto.GenerateInvitationResponse;
import com.unlam.verabackend.presentation.dto.InvitationDetailsResponse;
import com.unlam.verabackend.presentation.dto.ProtectedPersonResponse;

@ExtendWith(MockitoExtension.class)
public class TrustContactUseCaseImplTest {

    @Mock
    private TrustContactRepository trustContactRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private TrustInvitationRepository trustInvitationRepository;
    @Mock
    private NotificationService notificationService;
    @Mock
    private CloudinaryService cloudinaryService;

    @InjectMocks
    private TrustContactUseCaseImpl useCase;

    private User cuidador;
    private User protegido;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(useCase, "frontendUrl", "http://localhost:5173");

        cuidador = new User();
        ReflectionTestUtils.setField(cuidador, "id", 1L);
        cuidador.setEmail("cuidador@gmail.com");
        cuidador.setFullName("Juan Cuidador");

        protegido = new User();
        ReflectionTestUtils.setField(protegido, "id", 2L);
        protegido.setEmail("abuelo@gmail.com");
        protegido.setFullName("Pedro Abuelo");
    }

    @Test
    void deberiaLanzarExcepcionSiSeInvitaASiMismo() {
        GenerateInvitationRequest request = mock(GenerateInvitationRequest.class);
        when(request.getEmail()).thenReturn("cuidador@gmail.com");

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            useCase.generateInvitationLink(request, "cuidador@gmail.com");
        });
        assertEquals("No puedes generar una invitación para ti mismo", exception.getMessage());
    }

    @Test
    void deberiaGenerarInvitacionConExitoYEnviarNotificacionSiUsuarioExiste() {
        GenerateInvitationRequest request = mock(GenerateInvitationRequest.class);
        when(request.getEmail()).thenReturn("abuelo@gmail.com");
        when(request.getFullName()).thenReturn("Pedro Abuelo");
        when(request.getRelationship()).thenReturn("Abuelo");

        when(userRepository.findByEmail(cuidador.getEmail())).thenReturn(Optional.of(cuidador));

        TrustInvitation invitacionGuardada = TrustInvitation.builder()
                .id(10L).fullName("Pedro Abuelo").relationship("Abuelo").build();
        when(trustInvitationRepository.save(any())).thenReturn(invitacionGuardada);

        when(userRepository.findByEmail("abuelo@gmail.com")).thenReturn(Optional.of(protegido));

        GenerateInvitationResponse response = useCase.generateInvitationLink(request, cuidador.getEmail());

        assertNotNull(response);
        verify(trustInvitationRepository, times(1)).save(any(TrustInvitation.class));
        verify(notificationService, times(1)).createAndDispatch(eq(protegido), any(), anyString(), anyMap());
    }

    @Test
    void deberiaObtenerPersonasProtegidasCombinandoActivosYPendientes() {
        when(userRepository.findByEmail(cuidador.getEmail())).thenReturn(Optional.of(cuidador));

        TrustContact contactoActivo = TrustContact.builder()
                .id(100L).protectedUser(protegido).relationship("Padre").sensitivityLevel(SensitivityLevel.ALTO).notifyHighRisk(true).build();
        when(trustContactRepository.findByCarerId(cuidador.getId())).thenReturn(List.of(contactoActivo));

        TrustInvitation invitacionPendiente = TrustInvitation.builder()
                .id(200L).fullName("Tio Lucas").email("tio@gmail.com").status(InvitationStatus.PENDING).build();
        when(trustInvitationRepository.findByCarerIdAndStatus(cuidador.getId(), InvitationStatus.PENDING)).thenReturn(List.of(invitacionPendiente));

        List<ProtectedPersonResponse> resultado = useCase.getMyProtectedPeople(cuidador.getEmail());

        assertEquals(2, resultado.size());
        assertEquals("ACTIVE", resultado.get(0).getStatus());
        assertEquals("PENDING", resultado.get(1).getStatus());
    }

    @Test
    void deberiaLanzarExcepcionAlAceptarInvitacionExpirada() {
        String token = "token-viejo";
        TrustInvitation invitacionExpirada = TrustInvitation.builder()
                .status(InvitationStatus.PENDING)
                .expiresAt(LocalDateTime.now().minusDays(1))
                .build();

        when(trustInvitationRepository.findByToken(token)).thenReturn(Optional.of(invitacionExpirada));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            useCase.acceptInvitation(token, "abuelo@gmail.com");
        });
        assertEquals("El link de invitación expiró.", exception.getMessage());
        assertEquals(InvitationStatus.EXPIRED, invitacionExpirada.getStatus());
    }

    @Test
    void deberiaAceptarInvitacionPorIdConExito() {
        Long invId = 55L;
        TrustInvitation invitacion = TrustInvitation.builder()
                .id(invId)
                .carer(cuidador)
                .email(protegido.getEmail())
                .status(InvitationStatus.PENDING)
                .build();

        when(trustInvitationRepository.findById(invId)).thenReturn(Optional.of(invitacion));
        when(userRepository.findByEmail(protegido.getEmail())).thenReturn(Optional.of(protegido));
        when(trustContactRepository.existsByCarerIdAndProtectedUser_Id(cuidador.getId(), protegido.getId())).thenReturn(false);

        useCase.acceptInvitationById(invId, protegido.getEmail());

        verify(trustContactRepository, times(1)).save(any(TrustContact.class));
        assertEquals(InvitationStatus.ACCEPTED, invitacion.getStatus());
        verify(trustInvitationRepository, times(1)).save(invitacion);
        verify(notificationService, times(1)).createAndDispatch(eq(cuidador), any(), any(), any());
    }

    @Test
    void deberiaRechazarInvitacionPorIdConExito() {
        Long invId = 55L;
        TrustInvitation invitacion = TrustInvitation.builder()
                .id(invId)
                .carer(cuidador)
                .email(protegido.getEmail())
                .status(InvitationStatus.PENDING)
                .build();

        when(trustInvitationRepository.findById(invId)).thenReturn(Optional.of(invitacion));

        useCase.rejectInvitationById(invId, protegido.getEmail());

        assertEquals(InvitationStatus.REJECTED, invitacion.getStatus());
        verify(trustInvitationRepository, times(1)).save(invitacion);
        verify(notificationService, times(1)).createAndDispatch(eq(cuidador), any(), any(), any());
    }

    @Test
    void deberiaLanzarExcepcionAlActualizarConfiguracionConNivelInvalido() {
        assertThrows(RuntimeException.class, () -> {
            useCase.updateConfiguration(1L, "NIVEL_RANDOM", true);
        });
    }

    @Test
    void deberiaActualizarConfiguracionConExito() {
        TrustContact contacto = TrustContact.builder().sensitivityLevel(SensitivityLevel.BAJO).notifyHighRisk(false).build();
        when(trustContactRepository.findById(1L)).thenReturn(Optional.of(contacto));

        useCase.updateConfiguration(1L, "ALTO", true);

        assertEquals(SensitivityLevel.ALTO, contacto.getSensitivityLevel());
        assertTrue(contacto.isNotifyHighRisk());
        verify(trustContactRepository, times(1)).save(contacto);
    }

    @Test
    void deberiaActualizarInformacionDelProtegido() {
        TrustInvitation invitacion = TrustInvitation.builder()
                .carer(cuidador).status(InvitationStatus.PENDING).sensitivityLevel(SensitivityLevel.MEDIO)
                .build();
        when(trustInvitationRepository.findById(1L)).thenReturn(Optional.of(invitacion));

        ProtectedPersonResponse response = useCase.updateInformation(1L, "Nuevo Nombre", "Sobrino", "1234", "http://foto.jpg");

        assertEquals("Nuevo Nombre", invitacion.getFullName());
        assertEquals("http://foto.jpg", invitacion.getImage());
        verify(trustInvitationRepository, times(1)).save(invitacion);
        assertNotNull(response);
    }

    @Test
    void deberiaSubirImagenACloudinary() throws IOException {
        MultipartFile archivo = mock(MultipartFile.class);
        when(cloudinaryService.uploadImage(archivo, "protected")).thenReturn("http://nube.com/foto.jpg");

        String url = useCase.uploadProtectedPersonImage(archivo);

        assertEquals("http://nube.com/foto.jpg", url);
        verify(cloudinaryService, times(1)).uploadImage(archivo, "protected");
    }

    @Test
    void deberiaObtenerDetallesDeInvitacionConExito() {
        TrustInvitation inv = TrustInvitation.builder()
                .id(1L).fullName("Juan Cuidador").carer(cuidador).relationship("Amigo").status(InvitationStatus.PENDING).build();
        when(trustInvitationRepository.findByToken("token123")).thenReturn(Optional.of(inv));

        InvitationDetailsResponse response = useCase.getInvitationDetails("token123");
        assertEquals("Juan Cuidador", response.getCarerFullName());
    }

    @Test
    void deberiaLanzarExcepcionEnDetalleInvitacionSiNoEsPending() {
        TrustInvitation inv = TrustInvitation.builder().status(InvitationStatus.ACCEPTED).build();
        when(trustInvitationRepository.findByToken("token123")).thenReturn(Optional.of(inv));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> useCase.getInvitationDetails("token123"));
        assertEquals("Esta invitación ya fue utilizada o aceptada", exception.getMessage());
    }

    @Test
    void deberiaLanzarExcepcionEnDetalleInvitacionSiExpiro() {
        TrustInvitation inv = TrustInvitation.builder()
                .status(InvitationStatus.PENDING)
                .expiresAt(LocalDateTime.now().minusDays(1))
                .build();
        when(trustInvitationRepository.findByToken("token123")).thenReturn(Optional.of(inv));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> useCase.getInvitationDetails("token123"));
        assertEquals("El enlace de invitación ha expirado", exception.getMessage());
    }

    @Test
    void deberiaAceptarInvitacionPorTokenConExito() {
        TrustInvitation inv = TrustInvitation.builder()
                .carer(cuidador).status(InvitationStatus.PENDING).sensitivityLevel(SensitivityLevel.MEDIO)
                .build();

        when(trustInvitationRepository.findByToken("token123")).thenReturn(Optional.of(inv));
        when(userRepository.findByEmail(protegido.getEmail())).thenReturn(Optional.of(protegido));
        when(trustContactRepository.existsByCarerIdAndProtectedUser_Id(cuidador.getId(), protegido.getId())).thenReturn(false);

        useCase.acceptInvitation("token123", protegido.getEmail());

        assertEquals(InvitationStatus.ACCEPTED, inv.getStatus());
        verify(trustContactRepository, times(1)).save(any(TrustContact.class));
    }

    @Test
    void deberiaLanzarExcepcionAlAceptarPorTokenSiEsElMismoUsuario() {
        TrustInvitation inv = TrustInvitation.builder().carer(protegido).status(InvitationStatus.PENDING).build();
        when(trustInvitationRepository.findByToken("token123")).thenReturn(Optional.of(inv));
        when(userRepository.findByEmail(protegido.getEmail())).thenReturn(Optional.of(protegido));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> useCase.acceptInvitation("token123", protegido.getEmail()));
        assertEquals("No podés aceptar tu propia invitación de seguridad", exception.getMessage());
    }

    @Test
    void deberiaObtenerInvitacionesPendientesParaMiFiltrandoExpiradas() {
        TrustInvitation invValida = TrustInvitation.builder().id(1L).carer(cuidador).status(InvitationStatus.PENDING).build();
        TrustInvitation invExpirada = TrustInvitation.builder().id(2L).carer(cuidador).status(InvitationStatus.PENDING).expiresAt(LocalDateTime.now().minusDays(1)).build();

        when(trustInvitationRepository.findByEmailAndStatus("abuelo@gmail.com", InvitationStatus.PENDING))
                .thenReturn(List.of(invValida, invExpirada));

        List<InvitationDetailsResponse> response = useCase.getPendingInvitationsForMe("abuelo@gmail.com");

        assertEquals(1, response.size());
    }

    @Test
    void deberiaLanzarExcepcionAlAceptarPorIdSiEmailNoCoincide() {
        TrustInvitation inv = TrustInvitation.builder().id(1L).email("otro@gmail.com").status(InvitationStatus.PENDING).build();
        when(trustInvitationRepository.findById(1L)).thenReturn(Optional.of(inv));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> useCase.acceptInvitationById(1L, "abuelo@gmail.com"));
        assertEquals("No tenés permiso para operar esta invitación", exception.getMessage());
    }

    @Test
    void deberiaLanzarExcepcionAlAceptarPorIdSiYaEstanVinculados() {
        TrustInvitation inv = TrustInvitation.builder().id(1L).email(protegido.getEmail()).carer(cuidador).status(InvitationStatus.PENDING).build();
        when(trustInvitationRepository.findById(1L)).thenReturn(Optional.of(inv));
        when(userRepository.findByEmail(protegido.getEmail())).thenReturn(Optional.of(protegido));

        when(trustContactRepository.existsByCarerIdAndProtectedUser_Id(cuidador.getId(), protegido.getId())).thenReturn(true);

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> useCase.acceptInvitationById(1L, protegido.getEmail()));
        assertEquals("Ya existe una relación de cuidado activa con este usuario.", exception.getMessage());
    }

    @Test
    void deberiaObtenerMisCuidadores() {
        when(userRepository.findByEmail(protegido.getEmail())).thenReturn(Optional.of(protegido));
        TrustContact contacto = TrustContact.builder().carer(cuidador).protectedUser(protegido).build();

        when(trustContactRepository.findByProtectedUserId(protegido.getId())).thenReturn(List.of(contacto));

        List<CarerResponse> response = useCase.getMyCarers(protegido.getEmail());
        assertEquals(1, response.size());
    }

    @Test
    void deberiaActualizarInformacionSinImagen() {
        TrustInvitation inv = TrustInvitation.builder().carer(cuidador).status(InvitationStatus.PENDING).sensitivityLevel(SensitivityLevel.MEDIO).build();
        when(trustInvitationRepository.findById(1L)).thenReturn(Optional.of(inv));

        ProtectedPersonResponse response = useCase.updateInformation(1L, "Nombre", "Hijo", "123", "");

        assertNotNull(response);
        assertEquals("", inv.getImage());
        verify(trustInvitationRepository, times(1)).save(inv);
    }

    @Test
    void deberiaEliminarPersonaProtegidaActivaConExito() {
        String status = "ACTIVE";
        TrustContact contacto = TrustContact.builder().id(100L).carer(cuidador).build();

        when(userRepository.findByEmail(cuidador.getEmail())).thenReturn(Optional.of(cuidador));
        when(trustContactRepository.findById(100L)).thenReturn(Optional.of(contacto));

        useCase.deleteProtectedPerson(100L, status, cuidador.getEmail());

        verify(trustContactRepository, times(1)).delete(contacto);
    }

    @Test
    void deberiaEliminarPersonaProtegidaPendienteConExito() {
        String status = "PENDING";
        TrustInvitation invitacion = TrustInvitation.builder().id(200L).carer(cuidador).status(InvitationStatus.PENDING).build();

        when(userRepository.findByEmail(cuidador.getEmail())).thenReturn(Optional.of(cuidador));
        when(trustInvitationRepository.findById(200L)).thenReturn(Optional.of(invitacion));

        useCase.deleteProtectedPerson(200L, status, cuidador.getEmail());

        verify(trustInvitationRepository, times(1)).delete(invitacion);
    }

    @Test
    void deberiaLanzarExcepcionAlEliminarSiStatusEsInvalido() {
        when(userRepository.findByEmail(cuidador.getEmail())).thenReturn(Optional.of(cuidador));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            useCase.deleteProtectedPerson(100L, "INVALID_STATUS", cuidador.getEmail());
        });

        assertEquals("Status inválido: INVALID_STATUS", exception.getMessage());
    }

    @Test
    void deberiaObtenerPersonaProtegidaActivaPorId() {
        String status = "ACTIVE";
        TrustContact contacto = TrustContact.builder()
                .id(100L)
                .carer(cuidador)
                .protectedUser(protegido)
                .relationship("Abuelo")
                .sensitivityLevel(SensitivityLevel.ALTO)
                .build();

        when(userRepository.findByEmail(cuidador.getEmail())).thenReturn(Optional.of(cuidador));
        when(trustContactRepository.findById(100L)).thenReturn(Optional.of(contacto));

        ProtectedPersonResponse response = useCase.getProtectedPersonById(100L, status, cuidador.getEmail());

        assertNotNull(response);
        assertEquals(100L, response.getId());
        assertEquals("ACTIVE", response.getStatus());
        assertEquals("Pedro Abuelo", response.getFullName());
    }

    @Test
    void deberiaObtenerPersonaProtegidaPendientePorId() {
        String status = "PENDING";
        TrustInvitation invitacion = TrustInvitation.builder()
                .id(200L)
                .carer(cuidador)
                .fullName("Tio Lucas")
                .email("tio@gmail.com")
                .status(InvitationStatus.PENDING)
                .build();

        when(userRepository.findByEmail(cuidador.getEmail())).thenReturn(Optional.of(cuidador));
        when(trustInvitationRepository.findById(200L)).thenReturn(Optional.of(invitacion));

        ProtectedPersonResponse response = useCase.getProtectedPersonById(200L, status, cuidador.getEmail());

        assertNotNull(response);
        assertEquals(200L, response.getId());
        assertEquals("PENDING", response.getStatus());
        assertEquals("Tio Lucas", response.getFullName());
    }

    @Test
    void deberiaLanzarExcepcionAlObtenerSiStatusEsInvalido() {
        when(userRepository.findByEmail(cuidador.getEmail())).thenReturn(Optional.of(cuidador));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            useCase.getProtectedPersonById(100L, "STATUS_ERROR", cuidador.getEmail());
        });

        assertEquals("Status inválido: STATUS_ERROR", exception.getMessage());
    }
}