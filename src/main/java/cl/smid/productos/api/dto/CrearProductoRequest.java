package cl.smid.productos.api.dto;

import cl.smid.productos.dominio.modelo.TipoProducto;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Cuerpo de la peticion de creacion de un Producto. La sede y la unidad no se reciben: se heredan
 * del Caso o del contexto.
 */
@Schema(description = "Solicitud para crear un producto asociado a un caso.")
public record CrearProductoRequest(
        @Schema(description = "Identificador opaco del caso.", example = "7d6b3c3c-9d22-4b10-8d3d-1f3900c13f20",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "idCaso es obligatorio")
        @Size(max = 36, message = "idCaso no puede exceder 36 caracteres")
        String idCaso,

        @Schema(description = "Tipo de producto.", example = "INFORME",
                allowableValues = {"INFORME", "GESTION", "OFICIO", "DERIVACION", "RESOLUCION", "OTRO"},
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "tipo es obligatorio")
        TipoProducto tipo,

        @Schema(description = "Título del producto.", example = "Informe técnico sintético",
                requiredMode = Schema.RequiredMode.REQUIRED, maxLength = 200)
        @NotBlank(message = "titulo es obligatorio")
        @Size(max = 200, message = "titulo no puede exceder 200 caracteres")
        String titulo,

        @Schema(description = "Descripción sintética del producto.", example = "Descripción sintética del entregable.",
                maxLength = 2000)
        @Size(max = 2000, message = "descripcion no puede exceder 2000 caracteres")
        String descripcion
) {
}
