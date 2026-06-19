package cl.smid.productos.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.List;

/**
 * Representacion publica de un Producto. Enums como texto; identificadores opacos; sin llaves
 * internas. En el detalle se embeben las tareas y el historial; en los listados ambos se omiten
 * (nulos) para aligerar la respuesta.
 */
@Schema(description = "Representación pública de un producto. Expone solo identificadores opacos.")
public record ProductoResponse(
        @Schema(description = "Identificador opaco del producto.", example = "b2d9d6ec-f0de-4df7-aee6-3f7106e7da54")
        String altKey,
        @Schema(description = "Identificador opaco del caso asociado.", example = "7d6b3c3c-9d22-4b10-8d3d-1f3900c13f20")
        String idCaso,
        @Schema(description = "Tipo de producto.", example = "INFORME",
                allowableValues = {"INFORME", "GESTION", "OFICIO", "DERIVACION", "RESOLUCION", "OTRO"})
        String tipo,
        @Schema(description = "Título del producto.", example = "Informe técnico sintético")
        String titulo,
        @Schema(description = "Descripción sintética del producto.", example = "Descripción sintética del entregable.")
        String descripcion,
        @Schema(description = "Estado del producto.", example = "BORRADOR",
                allowableValues = {"BORRADOR", "EN_REVISION", "EMITIDO", "ANULADO"})
        String estado,
        @Schema(description = "Número oficial asignado al emitir.", example = "PRD-RM-1/2027")
        String numeroProducto,
        @Schema(description = "Identificador opaco de la sede.", example = "11111111-1111-1111-1111-111111111111")
        String idSede,
        @Schema(description = "Identificador opaco de la unidad.", example = "4f86e9a4-2924-41d7-bf27-6ef13b6f6b9a")
        String idUnidad,
        @Schema(description = "Identificador opaco del autor.", example = "c27f4500-f412-4fd1-86a8-6caa5933583b")
        String autor,
        @Schema(description = "Indica si el producto está vigente.", example = "true")
        boolean vigente,
        @Schema(description = "Instante de creación en UTC.", example = "2027-03-15T12:00:00Z")
        Instant creadoEn,
        @Schema(description = "Instante de última actualización en UTC.", example = "2027-03-16T09:30:00Z")
        Instant actualizadoEn,
        @Schema(description = "Instante de emisión en UTC.", example = "2027-03-20T10:00:00Z")
        Instant emitidoEn,
        @Schema(description = "Instante de anulación en UTC.", example = "2027-03-21T10:00:00Z")
        Instant anuladoEn,
        @Schema(description = "Tareas asociadas, presentes en el detalle.")
        List<TareaResponse> tareas,
        @Schema(description = "Historial del producto, presente en el detalle.")
        List<TransicionResponse> historial
) {
}
