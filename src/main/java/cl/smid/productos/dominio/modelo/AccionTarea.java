package cl.smid.productos.dominio.modelo;

/**
 * Acciones solicitables sobre una Tarea via el endpoint de transiciones.
 *
 * <ul>
 *   <li>{@link #TOMAR}, {@link #COMPLETAR}, {@link #CANCELAR}: avances de estado.</li>
 *   <li>{@link #REASIGNAR}: cambia el responsable sin alterar el estado.</li>
 * </ul>
 */
public enum AccionTarea {
    TOMAR,
    COMPLETAR,
    CANCELAR,
    REASIGNAR
}
