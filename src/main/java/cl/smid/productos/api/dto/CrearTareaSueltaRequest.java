package cl.smid.productos.api.dto;

import cl.smid.productos.dominio.modelo.PrioridadTarea;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/**
 * Cuerpo de la creacion de una Tarea suelta, ligada directamente a un Caso. La sede y la unidad se
 * heredan del Caso (enriquecimiento) o del contexto.
 */
@Schema(description = "Solicitud para crear una tarea suelta asociada directamente a un caso.")
public record CrearTareaSueltaRequest(
        @Schema(description = "Identificador opaco del caso.", example = "7d6b3c3c-9d22-4b10-8d3d-1f3900c13f20",
                requiredMode = Schema.RequiredMode.REQUIRED, maxLength = 36)
        @NotBlank(message = "idCaso es obligatorio")
        @Size(max = 36, message = "idCaso no puede exceder 36 caracteres")
        String idCaso,

        @Schema(description = "Título de la tarea.", example = "Solicitar antecedente sintético",
                requiredMode = Schema.RequiredMode.REQUIRED, maxLength = 200)
        @NotBlank(message = "titulo es obligatorio")
        @Size(max = 200, message = "titulo no puede exceder 200 caracteres")
        String titulo,

        @Schema(description = "Descripción operativa sintética.", example = "Descripción operativa sintética.",
                maxLength = 2000)
        @Size(max = 2000, message = "descripcion no puede exceder 2000 caracteres")
        String descripcion,

        @Schema(description = "Identificador opaco del responsable.", example = "c27f4500-f412-4fd1-86a8-6caa5933583b",
                maxLength = 36)
        @Size(max = 36, message = "responsableAlt no puede exceder 36 caracteres")
        String responsableAlt,

        @Schema(description = "Prioridad de la tarea.", example = "ALTA",
                allowableValues = {"BAJA", "MEDIA", "ALTA"},
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "prioridad es obligatoria")
        PrioridadTarea prioridad,

        @Schema(description = "Fecha de vencimiento.", example = "2027-03-30")
        LocalDate fechaVencimiento
) {
}
