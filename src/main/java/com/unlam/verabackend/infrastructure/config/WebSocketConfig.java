package com.unlam.verabackend.infrastructure.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Slf4j
@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final AuthChannelInterceptor authChannelInterceptor;

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        log.info("WebSocket Config: Inicializando Message Broker STOMP. Tópicos -> /topic | Prefijos -> /app");
        config.enableSimpleBroker("/topic");
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        log.info("WebSocket Config: Mapeando endpoint de entrada general [/ws-vera] con políticas CORS activas.");
        registry.addEndpoint("/ws-vera")
                .setAllowedOrigins("http://localhost:5173", "https://vera-frontend-gamma.vercel.app")
                .withSockJS();
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        log.debug("WebSocket Config: Enlazando middleware de interceptación de seguridad perimetral.");
        registration.interceptors(authChannelInterceptor);
    }
}