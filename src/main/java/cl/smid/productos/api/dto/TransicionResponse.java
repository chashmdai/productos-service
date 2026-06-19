package cl.smid.productos.api.dto;

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
public record TransicionResponse(
        String altKey,
        String estadoOrigen,
        String estadoDestino,
        String accion,
        String observacion,
        String actor,
        Instant ocurridoEn
) {
}
