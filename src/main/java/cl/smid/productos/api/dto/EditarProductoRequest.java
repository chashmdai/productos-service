package cl.smid.productos.api.dto;

import jakarta.validation.constraints.Size;

/**
 * Cuerpo de la edicion parcial de un Producto. Ambos campos son opcionales (merge de no nulos);
 * solo aplica en estados mutables.
 */
public record EditarProductoRequest(
        @Size(max = 200, message = "titulo no puede exceder 200 caracteres")
        String titulo,

        @Size(max = 2000, message = "descripcion no puede exceder 2000 caracteres")
        String descripcion
) {
}
