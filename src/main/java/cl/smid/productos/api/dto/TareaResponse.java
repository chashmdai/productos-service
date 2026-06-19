package cl.smid.productos.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

/**
 * Representacion publica de una Tarea. Enums como texto; identificadores opacos; sin llaves
 * internas. El historial solo se incluye en el detalle individual (omitido cuando es nulo).
 */
@Schema(description = "Representación pública de una tarea. Expone solo identificadores opacos.")
public record TareaResponse(
        @Schema(description = "Identificador opaco de la tarea.", example = "31abf51b-badf-4a3d-b2a7-1a37089e1370")
        String altKey,
        @Schema(description = "Identificador opaco del caso asociado.", example = "7d6b3c3c-9d22-4b10-8d3d-1f3900c13f20")
        String idCaso,
        @Schema(description = "Identificador opaco del producto padre, si aplica.", example = "b2d9d6ec-f0de-4df7-aee6-3f7106e7da54")
        String idProducto,
        @Schema(description = "Título de la tarea.", example = "Solicitar antecedente sintético")
        String titulo,
        @Schema(description = "Descripción operativa sintética.", example = "Descripción operativa sintética.")
        String descripcion,
        @Schema(description = "Identificador opaco del responsable.", example = "c27f4500-f412-4fd1-86a8-6caa5933583b")
        String responsableAlt,
        @Schema(description = "Nombre legible del responsable si el enriquecimiento está disponible.",
                example = "Responsable institucional")
        String responsableNombre,
        @Schema(description = "Prioridad de la tarea.", example = "ALTA", allowableValues = {"BAJA", "MEDIA", "ALTA"})
        String prioridad,
        @Schema(description = "Estado de la tarea.", example = "PENDIENTE",
                allowableValues = {"PENDIENTE", "EN_CURSO", "COMPLETADA", "CANCELADA"})
        String estado,
        @Schema(description = "Fecha de vencimiento.", example = "2027-03-30")
        LocalDate fechaVencimiento,
        @Schema(description = "Identificador opaco de la sede.", example = "11111111-1111-1111-1111-111111111111")
        String idSede,
        @Schema(description = "Identificador opaco de la unidad.", example = "4f86e9a4-2924-41d7-bf27-6ef13b6f6b9a")
        String idUnidad,
        @Schema(description = "Instante de creación en UTC.", example = "2027-03-15T12:00:00Z")
        Instant creadoEn,
        @Schema(description = "Instante de última actualización en UTC.", example = "2027-03-16T09:30:00Z")
        Instant actualizadoEn,
        @Schema(description = "Instante de completitud en UTC.", example = "2027-03-20T10:00:00Z")
        Instant completadoEn,
        @Schema(description = "Historial de la tarea, presente en el detalle individual.")
        List<TransicionResponse> historial
) {
}
