//package com.unlam.verabackend;
//import com.unlam.verabackend.domain.port.in.TrustContactUseCase;
//import com.unlam.verabackend.domain.model.SensitivityLevel;
//import com.unlam.verabackend.presentation.controller.TrustContactController;
//import com.unlam.verabackend.presentation.dto.GenerateInvitationRequest;
//import com.unlam.verabackend.presentation.dto.GenerateInvitationResponse;
//import com.unlam.verabackend.presentation.dto.InvitationDetailsResponse;
//
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.springframework.http.ResponseEntity;
//import org.springframework.security.core.Authentication;
//import java.util.UUID;
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.ArgumentMatchers.eq;
//import static org.mockito.Mockito.doNothing;
//import static org.mockito.Mockito.when;
//
//@ExtendWith(MockitoExtension.class)
//class TrustContactControllerTest {
//
//    @Mock
//    private TrustContactUseCase trustContactUseCase;
//
//    @Mock
//    private Authentication authentication;
//
//    private TrustContactController controller;
//
//    @BeforeEach
//    void setUp() {
//        controller = new TrustContactController(trustContactUseCase);
//    }
//
//
//    @Test
//    void generar_invitacion_exitosa_retorna_dto() {
//
//        // preparacion
//        GenerateInvitationRequest request = new GenerateInvitationRequest();
//        request.setFullName("María García");
//        request.setRelationship("Abuela");
//        request.setSensitivityLevel(SensitivityLevel.ALTO);
//
//        String fakeToken = UUID.randomUUID().toString();
//        GenerateInvitationResponse respuestaFalsaDelServicio = new GenerateInvitationResponse(fakeToken, "http://link.com/" + fakeToken);
//
//        when(authentication.getName()).thenReturn("tomas@email.com");
//        when(trustContactUseCase.generateInvitationLink(any(GenerateInvitationRequest.class), eq("tomas@email.com")))
//                .thenReturn(respuestaFalsaDelServicio);
//
//        // valores esperados
//        int statusEsperado = 200;
//        String tokenEsperado = fakeToken;
//        String linkEsperado = "http://link.com/" + fakeToken;
//
//        //ejecucion
//        ResponseEntity<GenerateInvitationResponse> respuestaReal = controller.generateInvitation(request, authentication);
//
//        //verificacion
//        assertEquals(statusEsperado, respuestaReal.getStatusCode().value());
//        assertInstanceOf(GenerateInvitationResponse.class, respuestaReal.getBody());
//
//        assertEquals(tokenEsperado, respuestaReal.getBody().getToken());
//        assertEquals(linkEsperado, respuestaReal.getBody().getInvitationLink());
//    }
//
//
//    @Test
//    void obtener_detalles_de_invitacion_retorna_dto() {
//
//        // preparacion
//        String token = "token-123";
//        InvitationDetailsResponse respuestaFalsaDelServicio = new InvitationDetailsResponse("María García", "Tomas Attino", "Abuela");
//
//        when(trustContactUseCase.getInvitationDetails(token)).thenReturn(respuestaFalsaDelServicio);
//
//        // valores esperados
//        int statusEsperado = 200;
//        String nombreProtegidoEsperado = "María García";
//        String nombreCuidadorEsperado = "Tomas Attino";
//        String relacionEsperada = "Abuela";
//
//        // ejecucion
//        ResponseEntity<InvitationDetailsResponse> respuestaReal = controller.getInvitationDetails(token);
//
//        // verificacion
//        assertEquals(statusEsperado, respuestaReal.getStatusCode().value());
//        assertInstanceOf(InvitationDetailsResponse.class, respuestaReal.getBody());
//
//        assertEquals(nombreProtegidoEsperado, respuestaReal.getBody().getProtectedFullName());
//        assertEquals(nombreCuidadorEsperado, respuestaReal.getBody().getCarerFullName());
//        assertEquals(relacionEsperada, respuestaReal.getBody().getRelationship());
//    }
//
//
//    @Test
//    void aceptar_invitacion_retorna_mensaje_de_exito() {
//
//        // preparacion
//        String token = "token-123";
//        when(authentication.getName()).thenReturn("abuela@email.com");
//        doNothing().when(trustContactUseCase).acceptInvitation(token, "abuela@email.com");
//
//        // valores esperados
//        int statusEsperado = 200;
//        String mensajeEsperado = "¡Invitación aceptada exitosamente! Ahora estás protegido.";
//
//        // ejecucion
//        ResponseEntity<String> respuestaReal = controller.acceptInvitation(token, authentication);
//
//        // verificacion
//        assertEquals(statusEsperado, respuestaReal.getStatusCode().value());
//        assertInstanceOf(String.class, respuestaReal.getBody());
//
//        assertEquals(mensajeEsperado, respuestaReal.getBody());
//    }
//}