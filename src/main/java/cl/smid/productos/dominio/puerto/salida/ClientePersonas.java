package cl.smid.productos.dominio.puerto.salida;

import java.util.Optional;

/**
 * Puerto de consulta al servicio de Personas (6.2) para resolver el nombre legible (no sensible)
 * de un responsable y almacenarlo como snapshot en la Tarea.
 *
 * <p>Con el enriquecimiento desactivado (defecto) la implementacion nula devuelve vacio y la Tarea
 * guarda el responsable sin nombre. La implementacion REST propaga el bearer y degrada a vacio
 * ante indisponibilidad.</p>
 */
public interface ClientePersonas {

    /** Nombre legible de la persona, o vacio si no es accesible/enriquecimiento desactivado. */
    Optional<String> nombreLegible(String idPersonaAlt);
}
