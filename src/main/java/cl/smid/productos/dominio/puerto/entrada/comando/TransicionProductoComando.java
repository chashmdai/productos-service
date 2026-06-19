package cl.smid.productos.dominio.puerto.entrada.comando;

import cl.smid.productos.dominio.modelo.AccionProducto;
import cl.smid.productos.dominio.modelo.ContextoSesion;

/**
 * Comando de transicion de estado de un Producto (ENVIAR_REVISION, EMITIR, ANULAR).
 *
 * @param contexto    contexto de sesion del solicitante (obligatorio).
 * @param altKey      identificador opaco del Producto (obligatorio).
 * @param accion      accion a aplicar (obligatoria).
 * @param observacion nota opcional del actor.
 */
public record TransicionProductoComando(
        ContextoSesion contexto,
        String altKey,
        AccionProducto accion,
        String observacion
) {
}
