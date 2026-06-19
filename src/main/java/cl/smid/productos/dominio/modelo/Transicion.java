package cl.smid.productos.dominio.modelo;

import java.time.Instant;
import java.util.Objects;

/**
 * Asiento inmutable del historial append-only de un agregado (Producto o Tarea).
 *
 * <p>Comparte estructura entre ambos agregados; los estados y la accion se guardan como
 * {@code String} (el {@code name()} del enum correspondiente) para reutilizar el tipo.
 * El asiento de creacion usa {@code accion = CREACION} y {@code estadoOrigen = null}.</p>
 *
 * @param altKey        identificador opaco del asiento.
 * @param estadoOrigen  estado previo ({@code null} en el asiento de creacion).
 * @param estadoDestino estado resultante.
 * @param accion        accion aplicada (incluye la pseudo-accion {@code CREACION}).
 * @param observacion   nota opcional aportada por el actor.
 * @param actor         alt_key del actor (usuario) o actor de sistema.
 * @param ocurridoEn    instante UTC del asiento.
 */
public record Transicion(
        String altKey,
        String estadoOrigen,
        String estadoDestino,
        String accion,
        String observacion,
        String actor,
        Instant ocurridoEn
) {
    /** Pseudo-accion del asiento de apertura del historial. */
    public static final String ACCION_CREACION = "CREACION";

    /** Actor de sistema (usado por el listener, que no porta usuario). */
    public static final String ACTOR_SISTEMA = "00000000-0000-0000-0000-000000000000";

    public Transicion {
        Objects.requireNonNull(altKey, "altKey del asiento es obligatorio");
        Objects.requireNonNull(estadoDestino, "estadoDestino es obligatorio");
        Objects.requireNonNull(accion, "accion es obligatoria");
        Objects.requireNonNull(actor, "actor es obligatorio");
        Objects.requireNonNull(ocurridoEn, "ocurridoEn es obligatorio");
    }
}
