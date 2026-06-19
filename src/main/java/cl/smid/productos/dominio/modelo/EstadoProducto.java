package cl.smid.productos.dominio.modelo;

/**
 * Estados del ciclo de vida de un Producto.
 *
 * <pre>
 *   BORRADOR ──ENVIAR_REVISION──▶ EN_REVISION ──EMITIR──▶ EMITIDO (terminal)
 *   BORRADOR / EN_REVISION ──ANULAR──▶ ANULADO (terminal)
 * </pre>
 *
 * El Producto es <strong>mutable</strong> (edicion de titulo/descripcion y alta de
 * tareas) solo en {@code BORRADOR} y {@code EN_REVISION}.
 */
public enum EstadoProducto {
    BORRADOR,
    EN_REVISION,
    EMITIDO,
    ANULADO;

    /** Indica si el estado admite edicion del Producto y alta de tareas hijas. */
    public boolean esMutable() {
        return this == BORRADOR || this == EN_REVISION;
    }

    /** Indica si el estado es terminal (no admite mas transiciones). */
    public boolean esTerminal() {
        return this == EMITIDO || this == ANULADO;
    }
}
