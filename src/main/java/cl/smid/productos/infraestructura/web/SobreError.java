package cl.smid.productos.infraestructura.web;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Sobre de error unificado del ecosistema SMID. Estructura comun a todos los servicios; el campo
 * de ruta se llama <strong>{@code ruta}</strong> (no {@code path}) por convencion del proyecto.
 *
 * <p>{@code detalles} solo se incluye en errores de validacion con desglose por campo; en el resto
 * se omite por ser nulo ({@link JsonInclude.Include#NON_NULL}).</p>
 *
 * @param status    estado HTTP.
 * @param error     titulo legible del error.
 * @param codigo    codigo estable de negocio (p. ej. {@code PRD-409}).
 * @param mensaje   descripcion breve.
 * @param detalles  desglose por campo (solo validacion; nulo en el resto).
 * @param ruta      ruta de la peticion.
 * @param timestamp instante UTC en ISO-8601.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Sobre de error unificado del ecosistema SMID.")
public record SobreError(
        @Schema(description = "Código HTTP numérico.", example = "409")
        int status,
        @Schema(description = "Título legible del error.", example = "Conflicto de estado")
        String error,
        @Schema(description = "Código estable del error.", example = "PRD-409",
                allowableValues = {"AUTZ-003", "AUTZ-004", "PRD-001", "PRD-404", "PRD-409",
                        "PRD-422", "PRD-500"})
        String codigo,
        @Schema(description = "Mensaje legible para el consumidor.",
                example = "No se puede editar un producto en estado EMITIDO")
        String mensaje,
        @Schema(description = "Detalle por campo, solo en errores de validación.",
                example = "[\"titulo: titulo es obligatorio\"]")
        List<String> detalles,
        @Schema(description = "Ruta solicitada.", example = "/productos/productos/b2d9d6ec-f0de-4df7-aee6-3f7106e7da54")
        String ruta,
        @Schema(description = "Instante UTC del error.", example = "2027-03-15T12:00:00Z")
        String timestamp
) {
    /** Construye un sobre fijando el {@code timestamp} al instante actual en ISO-8601 UTC. */
    public static SobreError de(int status, String error, String codigo, String mensaje,
                                List<String> detalles, String ruta) {
        return new SobreError(status, error, codigo, mensaje,
                (detalles == null || detalles.isEmpty()) ? null : List.copyOf(detalles),
                ruta, DateTimeFormatter.ISO_INSTANT.format(Instant.now()));
    }
}
