package cl.smid.productos.api.dto;

import cl.smid.productos.dominio.modelo.TipoProducto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Cuerpo de la peticion de creacion de un Producto. La sede y la unidad no se reciben: se heredan
 * del Caso o del contexto.
 */
public record CrearProductoRequest(
        @NotBlank(message = "idCaso es obligatorio")
        @Size(max = 36, message = "idCaso no puede exceder 36 caracteres")
        String idCaso,

        @NotNull(message = "tipo es obligatorio")
        TipoProducto tipo,

        @NotBlank(message = "titulo es obligatorio")
        @Size(max = 200, message = "titulo no puede exceder 200 caracteres")
        String titulo,

        @Size(max = 2000, message = "descripcion no puede exceder 2000 caracteres")
        String descripcion
) {
}
