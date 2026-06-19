package cl.smid.productos.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

/**
 * Cuerpo de la edicion parcial de un Producto. Ambos campos son opcionales (merge de no nulos);
 * solo aplica en estados mutables.
 */
@Schema(description = "Solicitud de edición parcial de un producto.")
public record EditarProductoRequest(
        @Schema(description = "Nuevo título del producto.", example = "Informe técnico actualizado", maxLength = 200)
        @Size(max = 200, message = "titulo no puede exceder 200 caracteres")
        String titulo,

        @Schema(description = "Nueva descripción sintética.", example = "Descripción sintética actualizada.",
                maxLength = 2000)
        @Size(max = 2000, message = "descripcion no puede exceder 2000 caracteres")
        String descripcion
) {
}
