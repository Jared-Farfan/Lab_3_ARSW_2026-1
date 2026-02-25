package edu.eci.arsw.blueprints.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * Configuración de WebSocket con STOMP.
 * 
 * STOMP (Simple Text Oriented Messaging Protocol) es un protocolo de mensajería
 * que funciona sobre WebSockets. Permite:
 * - Suscribirse a "topics" (canales) para recibir mensajes
 * - Enviar mensajes a destinos específicos
 * - Comunicación bidireccional en tiempo real
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    /*
     * Configura el endpoint de WebSocket y el broker de mensajes.
     * - El endpoint es la URL a la que los clientes se conectarán para usar
     * WebSocket.
     * - El broker maneja la distribución de mensajes a los suscriptores.
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws-blueprints").setAllowedOriginPatterns("http://localhost:5173");
        // .withSockJS(); // habilitar si quieres fallback
    }

    /*
     * Configura el broker de mensajes.
     * - enableSimpleBroker: Habilita un broker simple en memoria para manejar
     * topics y queues.
     * - setApplicationDestinationPrefixes: Prefijo para destinos de aplicación (ej:
     * /app/blueprints/create)
     * - setUserDestinationPrefix: Prefijo para destinos específicos de usuario (ej:
     * /user/queue/notifications)
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/topic", "/queue");
        registry.setApplicationDestinationPrefixes("/app");
        registry.setUserDestinationPrefix("/user");
    }
}
