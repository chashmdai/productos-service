package cl.smid.productos.api.dto;

import cl.smid.productos.dominio.modelo.AccionTarea;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Cuerpo de una transicion de Tarea (TOMAR, COMPLETAR, CANCELAR, REASIGNAR). En REASIGNAR el
 * {@code responsableAlt} es obligatorio (validado en el dominio: 422 si falta).
 */
@Schema(description = "Solicitud de transición de tarea. Coordinación o responsable actual pueden operar.")
public record TransicionTareaRequest(
        @Schema(description = "Acción de tarea.", example = "REASIGNAR",
                allowableValues = {"TOMAR", "COMPLETAR", "CANCELAR", "REASIGNAR"},
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "accion es obligatoria")
        AccionTarea accion,

        @Schema(description = "Identificador opaco del nuevo responsable. Obligatorio para REASIGNAR.",
                example = "c27f4500-f412-4fd1-86a8-6caa5933583b", maxLength = 36)
        @Size(max = 36, message = "responsableAlt no puede exceder 36 caracteres")
        String responsableAlt,

        @Schema(description = "Observación sintética de la transición.", example = "Reasignación sintética.",
                maxLength = 1000)
        @Size(max = 1000, message = "observacion no puede exceder 1000 caracteres")
        String observacion
) {
}
