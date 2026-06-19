package cl.smid.productos.api.dto;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

/**
 * Representacion publica de una Tarea. Enums como texto; identificadores opacos; sin llaves
 * internas. El historial solo se incluye en el detalle individual (omitido cuando es nulo).
 */
public record TareaResponse(
        String altKey,
        String idCaso,
        String idProducto,
        String titulo,
        String descripcion,
        String responsableAlt,
        String responsableNombre,
        String prioridad,
        String estado,
        LocalDate fechaVencimiento,
        String idSede,
        String idUnidad,
        Instant creadoEn,
        Instant actualizadoEn,
        Instant completadoEn,
        List<TransicionResponse> historial
) {
}
