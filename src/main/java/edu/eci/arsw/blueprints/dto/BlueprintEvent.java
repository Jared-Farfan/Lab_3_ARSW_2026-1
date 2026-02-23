package edu.eci.arsw.blueprints.dto;

import edu.eci.arsw.blueprints.model.Blueprint;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * DTO para eventos de WebSocket relacionados con blueprints.
 * Este record encapsula la información de un evento que será
 * enviado a través de WebSocket a todos los clientes suscritos.
 * Tipos de eventos:
 * CREATED: Un nuevo blueprint fue creado
 * UPDATED: Un blueprint existente fue modificado (ej: punto agregado)
 * DELETED: Un blueprint fue eliminado
 * @param eventType Tipo de evento (CREATED, UPDATED, DELETED)
 * @param blueprint El blueprint afectado (puede ser null en DELETE)
 * @param author Autor del blueprint
 * @param blueprintName Nombre del blueprint
 * @param message Mensaje descriptivo del evento
 */
@Schema(description = "Evento de WebSocket para cambios en blueprints")
public record BlueprintEvent(
    @Schema(description = "Tipo de evento", example = "CREATED")
    EventType eventType,
    
    @Schema(description = "Blueprint afectado (null en DELETE)")
    Blueprint blueprint,
    
    @Schema(description = "Autor del blueprint", example = "john")
    String author,
    
    @Schema(description = "Nombre del blueprint", example = "house")
    String blueprintName,
    
    @Schema(description = "Mensaje descriptivo", example = "Blueprint creado exitosamente")
    String message
) {
    /**
     * Tipos de eventos posibles.
     */
    public enum EventType {
        CREATED,
        UPDATED,
        DELETED  
    }
    
    /**
     * Crea un evento de creación de blueprint.
     */
    public static BlueprintEvent created(Blueprint bp) {
        return new BlueprintEvent(
            EventType.CREATED, 
            bp, 
            bp.getAuthor(), 
            bp.getName(),
            "Blueprint '%s' creado por %s".formatted(bp.getName(), bp.getAuthor())
        );
    }
    
    /**
     * Crea un evento de actualización de blueprint.
     */
    public static BlueprintEvent updated(Blueprint bp, String details) {
        return new BlueprintEvent(
            EventType.UPDATED, 
            bp, 
            bp.getAuthor(), 
            bp.getName(),
            "Blueprint '%s' actualizado: %s".formatted(bp.getName(), details)
        );
    }
    
    /**
     * Crea un evento de eliminación de blueprint.
     */
    public static BlueprintEvent deleted(String author, String name) {
        return new BlueprintEvent(
            EventType.DELETED, 
            null, 
            author, 
            name,
            "Blueprint '%s' de %s eliminado".formatted(name, author)
        );
    }
}
