package cl.smid.productos.infraestructura.web;

import com.fasterxml.jackson.annotation.JsonInclude;

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
public record SobreError(
        int status,
        String error,
        String codigo,
        String mensaje,
        List<String> detalles,
        String ruta,
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
