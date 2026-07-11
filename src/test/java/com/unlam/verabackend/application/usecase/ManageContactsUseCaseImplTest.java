package com.unlam.verabackend.application.usecase;

import com.unlam.verabackend.application.service.NotificationService;
import com.unlam.verabackend.domain.model.InvitationStatus;
import com.unlam.verabackend.domain.model.SensitivityLevel;
import com.unlam.verabackend.infrastructure.entity.TrustContact;
import com.unlam.verabackend.infrastructure.entity.TrustInvitation;
import com.unlam.verabackend.infrastructure.entity.User;
import com.unlam.verabackend.infrastructure.repository.TrustContactRepository;
import com.unlam.verabackend.infrastructure.repository.TrustInvitationRepository;
import com.unlam.verabackend.infrastructure.repository.UserRepository;
import com.unlam.verabackend.presentation.dto.AddContactRequest;
import com.unlam.verabackend.presentation.dto.ContactResponse;
import com.unlam.verabackend.presentation.dto.GenerateInvitationResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ManageContactsUseCaseImplTest {

    @Mock
    private TrustContactRepository trustContactRepository;
    @Mock
    private TrustInvitationRepository trustInvitationRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private ManageContactsUseCaseImpl useCase;

    private User cuidador;
    private User protegido;
    private User nuevoContacto;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(useCase, "frontendUrl", "http://localhost:5173");

        cuidador = new User();
        ReflectionTestUtils.setField(cuidador, "id", 1L);
        cuidador.setEmail("cuidador@gmail.com");

        protegido = new User();
        ReflectionTestUtils.setField(protegido, "id", 2L);
        protegido.setEmail("abuelo@gmail.com");

        nuevoContacto = new User();
        ReflectionTestUtils.setField(nuevoContacto, "id", 3L);
        nuevoContacto.setEmail("tio@gmail.com");
    }

    private void simularAccesoPermitido() {
        when(userRepository.findByEmail(cuidador.getEmail())).thenReturn(Optional.of(cuidador));
        when(trustContactRepository.existsByCarerIdAndProtectedUser_Id(cuidador.getId(), protegido.getId())).thenReturn(true);
    }

    private void simularAccesoDenegado() {
        when(userRepository.findByEmail(cuidador.getEmail())).thenReturn(Optional.of(cuidador));
        when(trustContactRepository.existsByCarerIdAndProtectedUser_Id(cuidador.getId(), protegido.getId())).thenReturn(false);
    }


    @Test
    void deberiaLanzarExcepcionSiCuidadorNoTieneAcceso() {

        simularAccesoDenegado();

        AccessDeniedException exception = assertThrows(AccessDeniedException.class, () -> {
            useCase.getContactsByProtectedPerson(protegido.getId(), cuidador.getEmail());
        });

        assertEquals("No tenés acceso a los contactos de esta persona protegida", exception.getMessage());
    }

    @Test
    void deberiaAgregarContactoConExito() {
        simularAccesoPermitido();
        AddContactRequest request = new AddContactRequest("tio@gmail.com", "Tio Carlos", "1122334455", "Familiar", SensitivityLevel.ALTO, true, true);

        when(userRepository.findByEmail(request.contactEmail())).thenReturn(Optional.of(nuevoContacto));
        when(userRepository.findById(protegido.getId())).thenReturn(Optional.of(protegido));
        
        when(trustContactRepository.existsByCarerIdAndProtectedUser_Id(nuevoContacto.getId(), protegido.getId())).thenReturn(false);

        TrustContact contactoGuardado = TrustContact.builder()
                .carer(nuevoContacto)
                .protectedUser(protegido)
                .build();
        
        when(trustContactRepository.save(any(TrustContact.class))).thenReturn(contactoGuardado);

        ContactResponse response = useCase.addContact(protegido.getId(), cuidador.getEmail(), request);

        assertNotNull(response);
        verify(trustContactRepository, times(1)).save(any(TrustContact.class));
    }

    @Test
    void deberiaLanzarExcepcionAlAgregarContactoSiYaExiste() {
        simularAccesoPermitido();
        AddContactRequest request = new AddContactRequest("tio@gmail.com", "Tio Carlos", "1122334455", "Familiar", SensitivityLevel.ALTO, true, true);

        when(userRepository.findByEmail(request.contactEmail())).thenReturn(Optional.of(nuevoContacto));
        when(userRepository.findById(protegido.getId())).thenReturn(Optional.of(protegido));
        
        when(trustContactRepository.existsByCarerIdAndProtectedUser_Id(nuevoContacto.getId(), protegido.getId())).thenReturn(true);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            useCase.addContact(protegido.getId(), cuidador.getEmail(), request);
        });

        assertEquals("Este usuario ya es contacto de confianza del protegido", exception.getMessage());
    }

    @Test
    void deberiaGenerarInvitacionConExito() {
        simularAccesoPermitido();
        AddContactRequest request = new AddContactRequest("vecino@gmail.com", "Vecino Juan", "1122334455", "Vecino", SensitivityLevel.BAJO, false, false);

        when(userRepository.findById(protegido.getId())).thenReturn(Optional.of(protegido));
        
        TrustInvitation invitacionGuardada = TrustInvitation.builder()
                .id(100L)
                .fullName("Vecino Juan")
                .relationship("Vecino")
                .build();
        when(trustInvitationRepository.save(any(TrustInvitation.class))).thenReturn(invitacionGuardada);

        when(userRepository.findByEmail(request.contactEmail())).thenReturn(Optional.empty());

        GenerateInvitationResponse response = useCase.inviteContact(protegido.getId(), cuidador.getEmail(), request);

        assertNotNull(response.getToken());
        assertTrue(response.getInvitationLink().startsWith("http://localhost:5173/invite/"));
        verify(trustInvitationRepository, times(1)).save(any(TrustInvitation.class));
        verify(notificationService, never()).createAndDispatch(any(), any(), any(), any());
    }

    @Test
    void deberiaEliminarContactoExistente() {
        Long contactId = 50L;
        TrustContact contactoMock = TrustContact.builder().protectedUser(protegido).build();

        when(trustContactRepository.existsById(contactId)).thenReturn(true);
        when(trustContactRepository.findById(contactId)).thenReturn(Optional.of(contactoMock));
        
        when(userRepository.findByEmail(cuidador.getEmail())).thenReturn(Optional.of(cuidador));
        when(trustContactRepository.existsByCarerIdAndProtectedUser_Id(cuidador.getId(), protegido.getId())).thenReturn(true);

        useCase.deleteContact(contactId, cuidador.getEmail());

        verify(trustContactRepository, times(1)).deleteById(contactId);
        verify(trustInvitationRepository, never()).deleteById(anyLong()); 
    }


    @Test
    void deberiaObtenerContactosActivosEInvitacionesPendientes() {
        simularAccesoPermitido();
        
        TrustContact contactoActivo = TrustContact.builder()
                .protectedUser(protegido)
                .carer(nuevoContacto)
                .build();
        when(trustContactRepository.findByProtectedUserId(protegido.getId()))
                .thenReturn(List.of(contactoActivo));

        TrustInvitation invitacionPendiente = TrustInvitation.builder()
                .status(InvitationStatus.PENDING)
                .expiresAt(LocalDateTime.now().plusDays(2)) 
                .build();
        when(trustInvitationRepository.findByProtectedPersonIdAndStatus(protegido.getId(), InvitationStatus.PENDING))
                .thenReturn(List.of(invitacionPendiente));

        List<ContactResponse> resultado = useCase.getContactsByProtectedPerson(protegido.getId(), cuidador.getEmail());

        assertEquals(2, resultado.size()); 
    }


    @Test
    void deberiaActualizarContactoConExito() {
        Long contactId = 15L;
        TrustContact contacto = TrustContact.builder()
                .protectedUser(protegido)
                .sensitivityLevel(SensitivityLevel.BAJO)
                .notifyHighRisk(false)
                .receiveAlertSummaries(false)
                .build();

        when(trustContactRepository.findById(contactId)).thenReturn(Optional.of(contacto));
        when(userRepository.findByEmail(cuidador.getEmail())).thenReturn(Optional.of(cuidador));
        when(trustContactRepository.existsByCarerIdAndProtectedUser_Id(cuidador.getId(), protegido.getId())).thenReturn(true);

        useCase.updateContact(contactId, cuidador.getEmail(), "ALTO", true, true);

        assertEquals(SensitivityLevel.ALTO, contacto.getSensitivityLevel());
        assertTrue(contacto.isNotifyHighRisk());
        assertTrue(contacto.isReceiveAlertSummaries());
        verify(trustContactRepository, times(1)).save(contacto);
    }

    @Test
    void deberiaLanzarExcepcionAlActualizarSiContactoNoExiste() {
        Long contactId = 15L;
        when(trustContactRepository.findById(contactId)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            useCase.updateContact(contactId, cuidador.getEmail(), "ALTO", true, true);
        });
        assertEquals("Contacto no encontrado", exception.getMessage());
    }


    @Test
    void deberiaEliminarInvitacionSiElIdNoPerteneceAUnContactoActivo() {
        Long contactId = 99L;
        
        when(trustContactRepository.existsById(contactId)).thenReturn(false);

        TrustInvitation invitacion = TrustInvitation.builder()
                .id(contactId)
                .protectedPerson(protegido)
                .build();
        when(trustInvitationRepository.findById(contactId)).thenReturn(Optional.of(invitacion));
        
        when(userRepository.findByEmail(cuidador.getEmail())).thenReturn(Optional.of(cuidador));
        when(trustContactRepository.existsByCarerIdAndProtectedUser_Id(cuidador.getId(), protegido.getId())).thenReturn(true);

        useCase.deleteContact(contactId, cuidador.getEmail());

        verify(trustInvitationRepository, times(1)).deleteById(contactId);
        verify(trustContactRepository, never()).deleteById(anyLong());
    }

    @Test
    void deberiaLanzarExcepcionAlEliminarSiNoExisteEnNingunaTabla() {
        Long contactId = 99L;
        when(trustContactRepository.existsById(contactId)).thenReturn(false);
        when(trustInvitationRepository.findById(contactId)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            useCase.deleteContact(contactId, cuidador.getEmail());
        });
        assertEquals("Contacto o invitación no encontrada", exception.getMessage());
    }


    @Test
    void deberiaGenerarInvitacionYEnviarNotificacionPushSiUsuarioYaEstaRegistrado() {
        simularAccesoPermitido();

        cuidador.setFullName("Juan Cuidador");
        
        AddContactRequest request = new AddContactRequest("amigo@gmail.com", "Amigo", "112233", "Amistad", SensitivityLevel.MEDIO, false, false);
        when(userRepository.findById(protegido.getId())).thenReturn(Optional.of(protegido));

        TrustInvitation invitacionGuardada = TrustInvitation.builder()
                .id(1L).fullName("Amigo").relationship("Amistad").build();
        when(trustInvitationRepository.save(any())).thenReturn(invitacionGuardada);

        User usuarioInvitadoExistente = new User();
        ReflectionTestUtils.setField(usuarioInvitadoExistente, "id", 5L);
        when(userRepository.findByEmail(request.contactEmail())).thenReturn(Optional.of(usuarioInvitadoExistente));

        useCase.inviteContact(protegido.getId(), cuidador.getEmail(), request);

        verify(notificationService, times(1)).createAndDispatch(eq(usuarioInvitadoExistente), any(), any(), any());
    }
}