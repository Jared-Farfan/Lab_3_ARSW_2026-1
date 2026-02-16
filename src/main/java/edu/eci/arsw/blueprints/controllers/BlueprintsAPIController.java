package edu.eci.arsw.blueprints.controllers;

import java.util.Set;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import edu.eci.arsw.blueprints.dto.ApiResponsEscheme;
import edu.eci.arsw.blueprints.model.Blueprint;
import edu.eci.arsw.blueprints.model.Point;
import edu.eci.arsw.blueprints.persistence.BlueprintNotFoundException;
import edu.eci.arsw.blueprints.persistence.BlueprintPersistenceException;
import edu.eci.arsw.blueprints.services.BlueprintsServices;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

/**
 * Controlador REST para gestionar blueprints.
 * Proporciona endpoints CRUD para diseños arquitectónicos.
 */
@RestController
@RequestMapping("/api/v1/blueprints")
@Tag(name = "Blueprints", description = "API de gestión de blueprints para diseños arquitectónicos")
public class BlueprintsAPIController {

        private final BlueprintsServices services;

        public BlueprintsAPIController(BlueprintsServices services) { this.services = services; }

        /**
         * Obtiene todos los blueprints del sistema.
         * @return ApiResponse con el conjunto de blueprints
         */
        @Operation(
                summary = "Obtener todos los blueprints",
                description = "Recupera la lista completa de blueprints almacenados en el sistema"
        )
        @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "200",
                description = "Blueprints recuperados exitosamente",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponsEscheme.class))
        )
        })
        @GetMapping
        public ResponseEntity<ApiResponsEscheme<Set<Blueprint>>> getAll() {
                return ResponseEntity.ok(ApiResponsEscheme.ok("Blueprints obtenidos exitosamente", services.getAllBlueprints()));
        }

        /**
         * Obtiene todos los blueprints de un autor específico.
         * @param author Nombre del autor
         * @return ApiResponse con los blueprints del autor
         */
        @Operation(
                summary = "Obtener blueprints por autor",
                description = "Recupera todos los blueprints creados por un autor específico"
        )
        @ApiResponses(value = {
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "200",
                        description = "Blueprints del autor recuperados exitosamente",
                        content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponsEscheme.class))
                ),
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "404",
                        description = "Autor no encontrado o sin blueprints",
                        content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponsEscheme.class))
                )
        })
        @GetMapping("/{author}")
        public ResponseEntity<ApiResponsEscheme<?>> byAuthor(
                @Parameter(description = "Nombre del autor del blueprint", required = true)
                @PathVariable String author) {
                try {
                        return ResponseEntity.ok(ApiResponsEscheme.ok("Blueprints del autor obtenidos", services.getBlueprintsByAuthor(author)));
                } catch (BlueprintNotFoundException e) {
                        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponsEscheme.notFound(e.getMessage()));
                }
        }

        /**
         * Obtiene un blueprint específico por autor y nombre.
         * @param author Nombre del autor
         * @param bpname Nombre del blueprint
         * @return ApiResponse con el blueprint solicitado
         */
        @Operation(
                summary = "Obtener blueprint por autor y nombre",
                description = "Recupera un blueprint específico identificado por su autor y nombre"
        )
        @ApiResponses(value = {
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "200",
                        description = "Blueprint recuperado exitosamente",
                        content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponsEscheme.class))
                ),
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "404",
                        description = "Blueprint no encontrado",
                        content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponsEscheme.class))
                )
        })
        @GetMapping("/{author}/{bpname}")
        public ResponseEntity<ApiResponsEscheme<?>> byAuthorAndName(
                @Parameter(description = "Nombre del autor del blueprint", required = true)
                @PathVariable String author,
                @Parameter(description = "Nombre del blueprint", required = true)
                @PathVariable String bpname) {
                try {
                        return ResponseEntity.ok(ApiResponsEscheme.ok("Blueprint obtenido exitosamente", services.getBlueprint(author, bpname)));
                } catch (BlueprintNotFoundException e) {
                        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponsEscheme.notFound(e.getMessage()));
                }
        }

        /**
         * Crea un nuevo blueprint en el sistema.
         * @param req Solicitud con autor, nombre y puntos del blueprint
         * @return ApiResponse con código 201 si es exitoso
         */
        @Operation(
                summary = "Crear un nuevo blueprint",
                description = "Agrega un nuevo blueprint al sistema con el autor, nombre y puntos especificados"
        )
        @ApiResponses(value = {
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "201",
                        description = "Blueprint creado exitosamente",
                        content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponsEscheme.class))
                ),
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "400",
                        description = "Solicitud inválida o blueprint ya existe",
                        content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponsEscheme.class))
                )
        })
        @PostMapping
        public ResponseEntity<ApiResponsEscheme<?>> add(
                @io.swagger.v3.oas.annotations.parameters.RequestBody(
                        description = "Datos del nuevo blueprint",
                        required = true,
                        content = @Content(schema = @Schema(implementation = NewBlueprintRequest.class))
                )
                @Valid @RequestBody NewBlueprintRequest req) {
                try {
                        Blueprint bp = new Blueprint(req.author(), req.name(), req.points());
                        services.addNewBlueprint(bp);
                        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponsEscheme.created("Blueprint creado exitosamente", bp));
                } catch (BlueprintPersistenceException e) {
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponsEscheme.badRequest(e.getMessage()));
                }
        }

        /**
         * Agrega un punto a un blueprint existente.
         * @param author Nombre del autor
         * @param bpname Nombre del blueprint
         * @param p Punto a agregar (coordenadas x, y)
         * @return ApiResponse con código 202 si es exitoso
         */
        @Operation(
                summary = "Agregar punto a un blueprint",
                description = "Agrega un nuevo punto de coordenadas a un blueprint existente"
        )
        @ApiResponses(value = {
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "202",
                        description = "Punto agregado exitosamente",
                        content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponsEscheme.class))
                ),
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "400",
                        description = "Datos del punto inválidos",
                        content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponsEscheme.class))
                ),
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "404",
                        description = "Blueprint no encontrado",
                        content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponsEscheme.class))
                )
        })
        @PutMapping("/{author}/{bpname}/points")
        public ResponseEntity<ApiResponsEscheme<?>> addPoint(
                @Parameter(description = "Nombre del autor del blueprint", required = true)
                @PathVariable String author,
                @Parameter(description = "Nombre del blueprint", required = true)
                @PathVariable String bpname,
                @io.swagger.v3.oas.annotations.parameters.RequestBody(
                        description = "Coordenadas del punto a agregar",
                        required = true,
                        content = @Content(schema = @Schema(implementation = Point.class))
                )
                @RequestBody Point p) {
                try {
                        services.addPoint(author, bpname, p.getX(), p.getY());
                        return ResponseEntity.status(HttpStatus.ACCEPTED).body(ApiResponsEscheme.accepted("Punto agregado exitosamente", p));
                } catch (BlueprintNotFoundException e) {
                        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponsEscheme.notFound(e.getMessage()));
                }
        }

        /**
         * Record para la solicitud de creación de blueprint.
         * @param author Autor del blueprint (requerido)
         * @param name Nombre del blueprint (requerido)
         * @param points Lista de puntos que definen el blueprint
         */
        @Schema(description = "Datos para crear un nuevo blueprint")
        public record NewBlueprintRequest(
                @Schema(description = "Autor del blueprint", example = "john_doe", requiredMode = Schema.RequiredMode.REQUIRED)
                @NotBlank String author,
                @Schema(description = "Nombre del blueprint", example = "modern_house", requiredMode = Schema.RequiredMode.REQUIRED)
                @NotBlank String name,
                @Schema(description = "Lista de puntos del blueprint")
                @Valid java.util.List<Point> points
        ) { }
}
