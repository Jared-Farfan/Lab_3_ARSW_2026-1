package edu.eci.arsw.blueprints.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Clase genérica de respuesta uniforme para la API REST.
 * 
 * Proporciona una estructura consistente para todas las respuestas del API,
 * facilitando el manejo de errores y éxitos en el cliente.
 *
 * @param <T> Tipo de dato contenido en la respuesta
 * @param code Código HTTP de la respuesta (200, 201, 400, 404, etc.)
 * @param message Mensaje descriptivo del resultado de la operación
 * @param data Datos de la respuesta (puede ser null en caso de error o respuestas sin contenido)
 */
@Schema(description = "Respuesta uniforme de la API")
public record ApiResponse<T>(
    @Schema(description = "Código HTTP de la respuesta", example = "200")
    int code,
    
    @Schema(description = "Mensaje descriptivo del resultado", example = "Operación exitosa")
    String message,
    
    @Schema(description = "Datos de la respuesta")
    T data
) {
    /**
     * Crea una respuesta exitosa con código 200.
     */
    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(200, "Operación exitosa", data);
    }

    /**
     * Crea una respuesta exitosa con código 200 y mensaje personalizado.
     */
    public static <T> ApiResponse<T> ok(String message, T data) {
        return new ApiResponse<>(200, message, data);
    }

    /**
     * Crea una respuesta de recurso creado con código 201.
     */
    public static <T> ApiResponse<T> created(T data) {
        return new ApiResponse<>(201, "Recurso creado exitosamente", data);
    }

    /**
     * Crea una respuesta de recurso creado con código 201 y mensaje personalizado.
     */
    public static <T> ApiResponse<T> created(String message, T data) {
        return new ApiResponse<>(201, message, data);
    }

    /**
     * Crea una respuesta de actualización aceptada con código 202.
     */
    public static <T> ApiResponse<T> accepted(T data) {
        return new ApiResponse<>(202, "Actualización aceptada", data);
    }

    /**
     * Crea una respuesta de actualización aceptada con código 202 y mensaje personalizado.
     */
    public static <T> ApiResponse<T> accepted(String message, T data) {
        return new ApiResponse<>(202, message, data);
    }

    /**
     * Crea una respuesta de error de solicitud inválida con código 400.
     */
    public static <T> ApiResponse<T> badRequest(String message) {
        return new ApiResponse<>(400, message, null);
    }

    /**
     * Crea una respuesta de recurso no encontrado con código 404.
     */
    public static <T> ApiResponse<T> notFound(String message) {
        return new ApiResponse<>(404, message, null);
    }

    /**
     * Crea una respuesta de error interno con código 500.
     */
    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(500, message, null);
    }
}
