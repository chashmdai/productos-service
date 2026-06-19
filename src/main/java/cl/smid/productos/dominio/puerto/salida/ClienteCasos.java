package cl.smid.productos.dominio.puerto.salida;

import cl.smid.productos.dominio.modelo.ResumenCaso;

import java.util.Optional;

/**
 * Puerto de consulta al servicio de Casos (6.4) para el enriquecimiento on-demand.
 *
 * <p>Con el enriquecimiento desactivado (defecto) la implementacion nula devuelve siempre vacio y
 * la herencia territorial proviene del contexto del usuario. Con el enriquecimiento activo, la
 * implementacion REST resuelve el resumen propagando el bearer del solicitante y degrada a vacio
 * ante indisponibilidad.</p>
 */
public interface ClienteCasos {

    /** Resumen del Caso, o vacio si no es accesible/no existe/enriquecimiento desactivado. */
    Optional<ResumenCaso> obtener(String idCasoAlt);
}
