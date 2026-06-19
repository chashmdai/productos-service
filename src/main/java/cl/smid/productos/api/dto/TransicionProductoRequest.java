package cl.smid.productos.api.dto;

import cl.smid.productos.dominio.modelo.AccionProducto;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Cuerpo de una transicion de Producto (ENVIAR_REVISION, EMITIR, ANULAR). */
public record TransicionProductoRequest(
        @NotNull(message = "accion es obligatoria")
        AccionProducto accion,

        @Size(max = 1000, message = "observacion no puede exceder 1000 caracteres")
        String observacion
) {
}
