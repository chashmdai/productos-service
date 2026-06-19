package cl.smid.productos.api.dto;

import cl.smid.productos.dominio.modelo.AccionTarea;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Cuerpo de una transicion de Tarea (TOMAR, COMPLETAR, CANCELAR, REASIGNAR). En REASIGNAR el
 * {@code responsableAlt} es obligatorio (validado en el dominio: 422 si falta).
 */
public record TransicionTareaRequest(
        @NotNull(message = "accion es obligatoria")
        AccionTarea accion,

        @Size(max = 36, message = "responsableAlt no puede exceder 36 caracteres")
        String responsableAlt,

        @Size(max = 1000, message = "observacion no puede exceder 1000 caracteres")
        String observacion
) {
}
