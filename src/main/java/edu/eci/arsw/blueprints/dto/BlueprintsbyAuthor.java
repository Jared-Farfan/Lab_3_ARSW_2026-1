package edu.eci.arsw.blueprints.dto;

import java.util.Set;

import edu.eci.arsw.blueprints.model.Blueprint;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Respuesta con blueprints de un autor y cantidad total")
public record BlueprintsbyAuthor(
        @Schema(description = "Nombre del autor", example = "john")
        String author,
        @Schema(description = "Cantidad total de blueprints del autor", example = "3")
        int totalBlueprints,
        @Schema(description = "Lista de blueprints del autor")
        Set<Blueprint> blueprints
) {
    public static BlueprintsbyAuthor of(String author, Set<Blueprint> blueprints) {
        return new BlueprintsbyAuthor(author, blueprints.size(), blueprints);
    }
}
