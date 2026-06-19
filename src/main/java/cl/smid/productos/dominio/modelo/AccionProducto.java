package cl.smid.productos.dominio.modelo;

/**
 * Acciones de transicion solicitables sobre un Producto.
 *
 * <p>{@link #EMITIR} y {@link #ANULAR} exigen rol de Coordinacion (override de negocio:
 * AUTZ-004 / 403 si falta). {@link #ENVIAR_REVISION} es operativa (solo alcance territorial).</p>
 */
public enum AccionProducto {
    ENVIAR_REVISION,
    EMITIR,
    ANULAR;

    /** {@code true} si la accion solo puede ejecutarla un perfil de Coordinacion. */
    public boolean exigeCoordinacion() {
        return this == EMITIR || this == ANULAR;
    }
}
