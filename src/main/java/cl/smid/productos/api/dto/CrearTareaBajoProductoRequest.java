package cl.smid.productos.api.dto;

import cl.smid.productos.dominio.modelo.PrioridadTarea;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/**
 * Cuerpo de la creacion de una Tarea bajo un Producto. El Caso, la sede y la unidad se heredan del
 * Producto padre (indicado en la ruta).
 */
public record CrearTareaBajoProductoRequest(
        @NotBlank(message = "titulo es obligatorio")
        @Size(max = 200, message = "titulo no puede exceder 200 caracteres")
        String titulo,

        @Size(max = 2000, message = "descripcion no puede exceder 2000 caracteres")
        String descripcion,

        @Size(max = 36, message = "responsableAlt no puede exceder 36 caracteres")
        String responsableAlt,

        @NotNull(message = "prioridad es obligatoria")
        PrioridadTarea prioridad,

        LocalDate fechaVencimiento
) {
}
