package cl.smid.productos.dominio.servicio;

import cl.smid.productos.dominio.excepcion.ConflictoEstado;
import cl.smid.productos.dominio.modelo.AccionProducto;
import cl.smid.productos.dominio.modelo.EstadoProducto;

/**
 * Maquina de estados del agregado Producto. Servicio de dominio puro y sin estado: dado el estado
 * actual y una accion, calcula el estado destino o rechaza la transicion.
 *
 * <pre>
 *   BORRADOR    --ENVIAR_REVISION--&gt; EN_REVISION
 *   EN_REVISION --EMITIR----------&gt; EMITIDO   (terminal)
 *   BORRADOR    --ANULAR----------&gt; ANULADO   (terminal)
 *   EN_REVISION --ANULAR----------&gt; ANULADO   (terminal)
 * </pre>
 *
 * <p>El control de rol (EMITIR/ANULAR exigen Coordinacion) y el territorio se evaluan en el
 * orquestador, no aqui: esta clase solo conoce la legalidad estructural de la transicion.</p>
 */
public class MaquinaEstadosProducto {

    /**
     * Calcula el estado resultante de aplicar {@code accion} sobre {@code actual}.
     *
     * @throws ConflictoEstado si la transicion no esta permitida desde el estado actual.
     */
    public EstadoProducto siguiente(EstadoProducto actual, AccionProducto accion) {
        return switch (accion) {
            case ENVIAR_REVISION -> {
                exigir(actual == EstadoProducto.BORRADOR, accion, actual);
                yield EstadoProducto.EN_REVISION;
            }
            case EMITIR -> {
                exigir(actual == EstadoProducto.EN_REVISION, accion, actual);
                yield EstadoProducto.EMITIDO;
            }
            case ANULAR -> {
                exigir(actual == EstadoProducto.BORRADOR || actual == EstadoProducto.EN_REVISION,
                        accion, actual);
                yield EstadoProducto.ANULADO;
            }
        };
    }

    private void exigir(boolean condicion, AccionProducto accion, EstadoProducto actual) {
        if (!condicion) {
            throw new ConflictoEstado(
                    "No se puede aplicar " + accion + " a un producto en estado " + actual);
        }
    }
}
