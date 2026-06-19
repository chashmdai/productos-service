package cl.smid.productos.api.dto;

import cl.smid.productos.dominio.modelo.AccionProducto;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Cuerpo de una transicion de Producto (ENVIAR_REVISION, EMITIR, ANULAR). */
@Schema(description = "Solicitud de transición de producto. EMITIR y ANULAR requieren rol de Coordinación.")
public record TransicionProductoRequest(
        @Schema(description = "Acción de producto.", example = "ENVIAR_REVISION",
                allowableValues = {"ENVIAR_REVISION", "EMITIR", "ANULAR"},
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "accion es obligatoria")
        AccionProducto accion,

        @Schema(description = "Observación sintética de la transición.", example = "Envío a revisión sintético.",
                maxLength = 1000)
        @Size(max = 1000, message = "observacion no puede exceder 1000 caracteres")
        String observacion
) {
}
