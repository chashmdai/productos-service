package cl.smid.productos.dominio.servicio;

import cl.smid.productos.dominio.excepcion.ConflictoEstado;
import cl.smid.productos.dominio.modelo.AccionTarea;
import cl.smid.productos.dominio.modelo.EstadoTarea;

/**
 * Maquina de estados del agregado Tarea. Servicio de dominio puro y sin estado.
 *
 * <pre>
 *   PENDIENTE --TOMAR-----&gt; EN_CURSO
 *   EN_CURSO  --COMPLETAR-&gt; COMPLETADA (terminal)
 *   PENDIENTE --CANCELAR--&gt; CANCELADA  (terminal)
 *   EN_CURSO  --CANCELAR--&gt; CANCELADA  (terminal)
 * </pre>
 *
 * <p>{@code REASIGNAR} no es una transicion de estado: el orquestador la resuelve por separado
 * (no altera el estado). Por eso esta maquina solo admite TOMAR/COMPLETAR/CANCELAR; invocarla con
 * REASIGNAR es un error de programacion.</p>
 */
public class MaquinaEstadosTarea {

    /**
     * Calcula el estado resultante de aplicar {@code accion} sobre {@code actual}.
     *
     * @throws ConflictoEstado          si la transicion no esta permitida desde el estado actual.
     * @throws IllegalArgumentException si se invoca con {@code REASIGNAR} (no es transicion de estado).
     */
    public EstadoTarea siguiente(EstadoTarea actual, AccionTarea accion) {
        return switch (accion) {
            case TOMAR -> {
                exigir(actual == EstadoTarea.PENDIENTE, accion, actual);
                yield EstadoTarea.EN_CURSO;
            }
            case COMPLETAR -> {
                exigir(actual == EstadoTarea.EN_CURSO, accion, actual);
                yield EstadoTarea.COMPLETADA;
            }
            case CANCELAR -> {
                exigir(actual == EstadoTarea.PENDIENTE || actual == EstadoTarea.EN_CURSO, accion, actual);
                yield EstadoTarea.CANCELADA;
            }
            case REASIGNAR -> throw new IllegalArgumentException(
                    "REASIGNAR no es una transicion de estado; debe resolverse en el orquestador");
        };
    }

    private void exigir(boolean condicion, AccionTarea accion, EstadoTarea actual) {
        if (!condicion) {
            throw new ConflictoEstado(
                    "No se puede aplicar " + accion + " a una tarea en estado " + actual);
        }
    }
}
