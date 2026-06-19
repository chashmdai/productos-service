package cl.smid.productos.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

/**
 * Asiento del historial expuesto al cliente. Identificadores opacos; estados/accion como texto.
 *
 * @param altKey        identificador opaco del asiento.
 * @param estadoOrigen  estado previo ({@code null} en la creacion).
 * @param estadoDestino estado resultante.
 * @param accion        accion aplicada.
 * @param observacion   nota del actor (si la hubo).
 * @param actor         alt_key del actor.
 * @param ocurridoEn    instante UTC (ISO-8601).
 */
@Schema(description = "Asiento de historial de producto o tarea.")
public record TransicionResponse(
        @Schema(description = "Identificador opaco del asiento.", example = "afcf2d25-190c-4e37-aef8-9dd1543d29b0")
        String altKey,
        @Schema(description = "Estado previo. Nulo en el asiento de creación.", example = "BORRADOR")
        String estadoOrigen,
        @Schema(description = "Estado resultante.", example = "EN_REVISION")
        String estadoDestino,
        @Schema(description = "Acción aplicada.", example = "ENVIAR_REVISION")
        String accion,
        @Schema(description = "Observación sintética del actor.", example = "Envío a revisión sintético.")
        String observacion,
        @Schema(description = "Identificador opaco del actor.", example = "c27f4500-f412-4fd1-86a8-6caa5933583b")
        String actor,
        @Schema(description = "Instante UTC del asiento.", example = "2027-03-15T12:00:00Z")
        Instant ocurridoEn
) {
}
