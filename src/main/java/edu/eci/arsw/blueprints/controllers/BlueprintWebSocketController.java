package edu.eci.arsw.blueprints.controllers;

import java.util.List;

import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import edu.eci.arsw.blueprints.dto.BlueprintEvent;
import edu.eci.arsw.blueprints.model.Blueprint;
import edu.eci.arsw.blueprints.model.Point;
import edu.eci.arsw.blueprints.persistence.BlueprintNotFoundException;
import edu.eci.arsw.blueprints.persistence.BlueprintPersistenceException;
import edu.eci.arsw.blueprints.services.BlueprintsServices;

/**
 * Controlador WebSocket/STOMP para blueprints.
 * maneja la comunicación en tiempo real usando STOMP sobre WebSocket.
 */
@Controller  // No @RestController porque STOMP maneja la serialización
public class BlueprintWebSocketController {

    private final BlueprintsServices services;
    private final SimpMessagingTemplate messagingTemplate;

    public BlueprintWebSocketController(BlueprintsServices services, 
                                        SimpMessagingTemplate messagingTemplate) {
        this.services = services;
        this.messagingTemplate = messagingTemplate;
    }

    /**
     * Endpoint principal para dibujo colaborativo en tiempo real.
     * Cliente envía a: /app/draw
     * Servidor envía a: /topic/blueprints.{author}.{name}
     * Payload esperado: { author: "john", name: "house", point: { x: 10, y: 20 } }
     * El cliente se suscribe a /topic/blueprints.{author}.{name} para recibir
     * actualizaciones solo del blueprint específico que está editando.
     */
    @MessageMapping("/draw")
    public void draw(DrawMessage msg) {
        try {
            services.addPoint(msg.author(), msg.name(), msg.point().getX(), msg.point().getY());
            Blueprint bp = services.getBlueprint(msg.author(), msg.name());
            String topic = "/topic/blueprints.%s.%s".formatted(msg.author(), msg.name());
            messagingTemplate.convertAndSend(topic, bp);
            
        } catch (BlueprintNotFoundException e) {
            System.err.println("Blueprint no encontrado: " + e.getMessage());
        }
    }

    /**
     * Record para el mensaje de dibujo.
     */
    public record DrawMessage(String author, String name, Point point) {}

    /**
     * Crea un nuevo blueprint via WebSocket.
     * @param request Datos del nuevo blueprint
     * @return Evento de creación para broadcast
     */
    @MessageMapping("/blueprints/create")  // Recibe de /app/blueprints/create
    @SendTo("/topic/blueprints")           // Envía a todos los suscritos a /topic/blueprints
    public BlueprintEvent createBlueprint(CreateBlueprintMessage request) {
        try {
            Blueprint bp = new Blueprint(request.author(), request.name(), request.points());
            services.addNewBlueprint(bp);
            notifyAuthorTopic(request.author(), BlueprintEvent.created(bp));
            
            return BlueprintEvent.created(bp);
        } catch (BlueprintPersistenceException e) {
            return new BlueprintEvent(
                BlueprintEvent.EventType.CREATED,
                null,
                request.author(),
                request.name(),
                "Error: " + e.getMessage()
            );
        }
    }

    /**
     * Elimina un blueprint via WebSocket.
     * @param author Autor del blueprint
     * @param name Nombre del blueprint
     * @return Evento de eliminación
     */
    @MessageMapping("/blueprints/{author}/{name}/delete")
    @SendTo("/topic/blueprints")
    public BlueprintEvent deleteBlueprint(
            @DestinationVariable String author,
            @DestinationVariable String name) {
        try {
            services.deleteBlueprint(author, name);
            
            BlueprintEvent event = BlueprintEvent.deleted(author, name);
            notifyAuthorTopic(author, event);
            
            return event;
        } catch (BlueprintNotFoundException e) {
            return new BlueprintEvent(
                BlueprintEvent.EventType.DELETED,
                null,
                author,
                name,
                "Error: " + e.getMessage()
            );
        }
    }

    /**
     * Envía un evento a todos los suscritos de /topic/blueprints.
     * Este método es público para ser usado desde BlueprintsAPIController.
     * 
     * Cuando alguien crea un blueprint via REST API, también queremos
     * notificar a los clientes WebSocket.
     * 
     * @param event Evento a enviar
     */
    public void broadcastEvent(BlueprintEvent event) {
        // convertAndSend() serializa el objeto a JSON y lo envía al topic
        messagingTemplate.convertAndSend("/topic/blueprints", event);
        
        // También notificamos al topic del autor específico
        if (event.author() != null) {
            notifyAuthorTopic(event.author(), event);
        }
    }

    /**
     * Notifica al topic específico de un autor.
     * Los clientes suscritos a /topic/blueprints/{author} recibirán el mensaje.
     */
    private void notifyAuthorTopic(String author, BlueprintEvent event) {
        messagingTemplate.convertAndSend("/topic/blueprints/" + author, event);
    }

    /**
     * Record para el mensaje de creación de blueprint.
     */
    public record CreateBlueprintMessage(
        String author,
        String name,
        List<Point> points
    ) {}
}
