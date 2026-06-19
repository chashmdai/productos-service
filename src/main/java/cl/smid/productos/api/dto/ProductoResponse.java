package cl.smid.productos.api.dto;

import java.time.Instant;
import java.util.List;

/**
 * Representacion publica de un Producto. Enums como texto; identificadores opacos; sin llaves
 * internas. En el detalle se embeben las tareas y el historial; en los listados ambos se omiten
 * (nulos) para aligerar la respuesta.
 */
public record ProductoResponse(
        String altKey,
        String idCaso,
        String tipo,
        String titulo,
        String descripcion,
        String estado,
        String numeroProducto,
        String idSede,
        String idUnidad,
        String autor,
        boolean vigente,
        Instant creadoEn,
        Instant actualizadoEn,
        Instant emitidoEn,
        Instant anuladoEn,
        List<TareaResponse> tareas,
        List<TransicionResponse> historial
) {
}
