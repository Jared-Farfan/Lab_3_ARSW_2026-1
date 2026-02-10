package edu.eci.arsw.blueprints.controllers;

import java.util.Map;
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

import edu.eci.arsw.blueprints.model.Blueprint;
import edu.eci.arsw.blueprints.model.Point;
import edu.eci.arsw.blueprints.persistence.BlueprintNotFoundException;
import edu.eci.arsw.blueprints.persistence.BlueprintPersistenceException;
import edu.eci.arsw.blueprints.services.BlueprintsServices;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

/**
 * REST API Controller for managing blueprints.
 * Provides endpoints for CRUD operations on architectural blueprints.
 */
@RestController
@RequestMapping("/blueprints")
@Tag(name = "Blueprints", description = "Blueprints management API for architectural designs")
public class BlueprintsAPIController {

    private final BlueprintsServices services;

    public BlueprintsAPIController(BlueprintsServices services) { this.services = services; }

    /**
     * Retrieves all blueprints available in the system.
     * @return Set of all blueprints
     */
    @Operation(
            summary = "Get all blueprints",
            description = "Retrieves the complete list of all blueprints stored in the system"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Successfully retrieved all blueprints",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = Blueprint.class))
                    )
            )
    })
    @GetMapping
    public ResponseEntity<Set<Blueprint>> getAll() {
        return ResponseEntity.ok(services.getAllBlueprints());
    }

    /**
     * Retrieves all blueprints created by a specific author.
     * @param author The name of the blueprint author
     * @return Set of blueprints by the specified author
     */
    @Operation(
            summary = "Get blueprints by author",
            description = "Retrieves all blueprints created by a specific author"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Successfully retrieved blueprints for the author",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = Blueprint.class))
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Author not found or has no blueprints",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(example = "{\"error\": \"Author not found\"}")
                    )
            )
    })
    @GetMapping("/{author}")
    public ResponseEntity<?> byAuthor(
            @Parameter(description = "Name of the blueprint author", required = true)
            @PathVariable String author) {
        try {
            return ResponseEntity.ok(services.getBlueprintsByAuthor(author));
        } catch (BlueprintNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Retrieves a specific blueprint by author and blueprint name.
     * @param author The name of the blueprint author
     * @param bpname The name of the blueprint
     * @return The requested blueprint
     */
    @Operation(
            summary = "Get blueprint by author and name",
            description = "Retrieves a specific blueprint identified by its author and name"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Successfully retrieved the blueprint",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Blueprint.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Blueprint not found for the given author and name",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(example = "{\"error\": \"Blueprint not found\"}")
                    )
            )
    })
    @GetMapping("/{author}/{bpname}")
    public ResponseEntity<?> byAuthorAndName(
            @Parameter(description = "Name of the blueprint author", required = true)
            @PathVariable String author,
            @Parameter(description = "Name of the blueprint", required = true)
            @PathVariable String bpname) {
        try {
            return ResponseEntity.ok(services.getBlueprint(author, bpname));
        } catch (BlueprintNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Creates a new blueprint in the system.
     * @param req The blueprint creation request containing author, name, and points
     * @return HTTP 201 on success, HTTP 403 if blueprint already exists
     */
    @Operation(
            summary = "Create a new blueprint",
            description = "Adds a new blueprint to the system with the specified author, name, and points"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Blueprint successfully created",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request body - validation failed for author, name, or points",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(example = "{\"error\": \"Validation failed\"}")
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Blueprint already exists with the same author and name",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(example = "{\"error\": \"Blueprint already exists\"}")
                    )
            )
    })
    @PostMapping
    public ResponseEntity<?> add(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Blueprint creation request",
                    required = true,
                    content = @Content(schema = @Schema(implementation = NewBlueprintRequest.class))
            )
            @Valid @RequestBody NewBlueprintRequest req) {
        try {
            Blueprint bp = new Blueprint(req.author(), req.name(), req.points());
            services.addNewBlueprint(bp);
            return ResponseEntity.status(HttpStatus.CREATED).build();
        } catch (BlueprintPersistenceException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Adds a new point to an existing blueprint.
     * @param author The name of the blueprint author
     * @param bpname The name of the blueprint
     * @param p The point to add (x, y coordinates)
     * @return HTTP 202 on success, HTTP 404 if blueprint not found
     */
    @Operation(
            summary = "Add a point to a blueprint",
            description = "Adds a new coordinate point to an existing blueprint"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "202",
                    description = "Point successfully added to the blueprint",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid point data - missing or invalid x/y coordinates",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(example = "{\"error\": \"Invalid point coordinates\"}")
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Blueprint not found for the given author and name",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(example = "{\"error\": \"Blueprint not found\"}")
                    )
            )
    })
    @PutMapping("/{author}/{bpname}/points")
    public ResponseEntity<?> addPoint(
            @Parameter(description = "Name of the blueprint author", required = true)
            @PathVariable String author,
            @Parameter(description = "Name of the blueprint", required = true)
            @PathVariable String bpname,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Point coordinates to add",
                    required = true,
                    content = @Content(schema = @Schema(implementation = Point.class))
            )
            @RequestBody Point p) {
        try {
            services.addPoint(author, bpname, p.x(), p.y());
            return ResponseEntity.status(HttpStatus.ACCEPTED).build();
        } catch (BlueprintNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Request object for creating a new blueprint.
     * @param author The author of the blueprint (required, non-blank)
     * @param name The name of the blueprint (required, non-blank)
     * @param points The list of points defining the blueprint
     */
    @Schema(description = "Request body for creating a new blueprint")
    public record NewBlueprintRequest(
            @Schema(description = "Author of the blueprint", example = "john_doe", requiredMode = Schema.RequiredMode.REQUIRED)
            @NotBlank String author,
            @Schema(description = "Name of the blueprint", example = "modern_house", requiredMode = Schema.RequiredMode.REQUIRED)
            @NotBlank String name,
            @Schema(description = "List of coordinate points defining the blueprint")
            @Valid java.util.List<Point> points
    ) { }
}
