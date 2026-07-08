package com.unlam.verabackend.infrastructure.config;

import com.unlam.verabackend.application.service.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageDeliveryException;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuthChannelInterceptor implements ChannelInterceptor {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    private static final String BEARER_PREFIX = "Bearer ";
    private static final String AUTH_HEADER_NAME = "Authorization";
    private static final String SESSION_KEY_EMAIL = "userEmail";

    @Override
    public Message<?> preSend(@NonNull Message<?> message, @NonNull MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
            log.debug("WebSocket Interceptor: Validando handshake inicial de conexión entrante.");
            authenticateStompConnection(accessor, message);
        }

        return message;
    }

    private void authenticateStompConnection(StompHeaderAccessor accessor, Message<?> message) {
        String authHeader = accessor.getFirstNativeHeader(AUTH_HEADER_NAME);

        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            log.warn("WebSocket Security: Intento de conexión STOMP rechazado. Cabecera Bearer ausente.");
            throw new MessageDeliveryException(message, "Acceso denegado: Se requiere autenticación Bearer.");
        }

        String token = authHeader.substring(BEARER_PREFIX.length());

        try {
            String email = jwtService.extractUsername(token);

            if (email == null) {
                throw new MessageDeliveryException(message, "Token corrupto o sin identidad asociada.");
            }

            UserDetails userDetails = userDetailsService.loadUserByUsername(email);

            if (!jwtService.isTokenValid(token, userDetails)) {
                log.warn("WebSocket Security: Token inválido o expirado para el usuario [{}].", email);
                throw new MessageDeliveryException(message, "Token de WebSocket inválido.");
            }

            injectAuthenticationIntoSession(accessor, userDetails, email);

        } catch (Exception e) {
            log.error("WebSocket Security Error: Falla en la validación criptográfica en tiempo real. Motivo: {}", e.getMessage());
            throw new MessageDeliveryException(message, "Fallo de autenticación en canal de comunicación activa: " + e.getMessage());
        }
    }

    private void injectAuthenticationIntoSession(StompHeaderAccessor accessor, UserDetails userDetails, String email) {
        log.info("WebSocket Interceptor: Handshake exitoso. Sesión autorizada para el usuario [{}]", email);

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

        accessor.setUser(authentication);

        Map<String, Object> sessionAttributes = accessor.getSessionAttributes();
        if (sessionAttributes != null) {
            sessionAttributes.put(SESSION_KEY_EMAIL, email);
        }
    }
}