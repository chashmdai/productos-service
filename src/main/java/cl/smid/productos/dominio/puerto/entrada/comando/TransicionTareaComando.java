package cl.smid.productos.dominio.puerto.entrada.comando;

import cl.smid.productos.dominio.modelo.AccionTarea;
import cl.smid.productos.dominio.modelo.ContextoSesion;

/**
 * Comando de transicion de una Tarea (TOMAR, COMPLETAR, CANCELAR, REASIGNAR). En REASIGNAR el
 * campo {@code responsableAlt} es obligatorio; en el resto se ignora.
 *
 * @param contexto       contexto de sesion del solicitante (obligatorio).
 * @param altKey         identificador opaco de la Tarea (obligatorio).
 * @param accion         accion a aplicar (obligatoria).
 * @param responsableAlt nuevo responsable (obligatorio solo en REASIGNAR).
 * @param observacion    nota opcional del actor.
 */
public record TransicionTareaComando(
        ContextoSesion contexto,
        String altKey,
        AccionTarea accion,
        String responsableAlt,
        String observacion
) {
}
