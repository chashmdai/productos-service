package cl.smid.productos.dominio.modelo;

/**
 * Estados del ciclo de vida de una Tarea.
 *
 * <pre>
 *   PENDIENTE ──TOMAR──▶ EN_CURSO ──COMPLETAR──▶ COMPLETADA (terminal)
 *   PENDIENTE / EN_CURSO ──CANCELAR──▶ CANCELADA (terminal)
 * </pre>
 *
 * La reasignacion (cambio de responsable) no altera el estado.
 */
public enum EstadoTarea {
    PENDIENTE,
    EN_CURSO,
    COMPLETADA,
    CANCELADA;

    /** {@code true} mientras la Tarea admite avance o reasignacion. */
    public boolean esVigente() {
        return this == PENDIENTE || this == EN_CURSO;
    }

    /** {@code true} si el estado es terminal. */
    public boolean esTerminal() {
        return this == COMPLETADA || this == CANCELADA;
    }
}
