package cl.smid.productos.dominio.puerto.entrada.comando;

import cl.smid.productos.dominio.modelo.ContextoSesion;
import cl.smid.productos.dominio.modelo.PrioridadTarea;

import java.time.LocalDate;

/**
 * Comando de creacion de una Tarea. Sirve a los dos flujos:
 * <ul>
 *   <li><b>Bajo Producto:</b> {@code idProductoAlt} obligatorio; el Caso, la sede y la unidad se
 *       heredan del Producto padre.</li>
 *   <li><b>Suelta:</b> {@code idProductoAlt} nulo y {@code idCasoAlt} obligatorio; la sede y la
 *       unidad se heredan del Caso (enriquecimiento) o del contexto del usuario (defecto).</li>
 * </ul>
 *
 * @param contexto         contexto de sesion del autor (obligatorio).
 * @param idCasoAlt        Caso (obligatorio en el flujo suelto; derivado del Producto en el otro).
 * @param idProductoAlt    Producto padre (obligatorio en el flujo bajo Producto; nulo en el suelto).
 * @param titulo           titulo de la Tarea (obligatorio).
 * @param descripcion      descripcion (opcional).
 * @param responsableAlt   responsable inicial (opcional).
 * @param prioridad        prioridad (obligatoria).
 * @param fechaVencimiento fecha limite (opcional).
 */
public record CrearTareaComando(
        ContextoSesion contexto,
        String idCasoAlt,
        String idProductoAlt,
        String titulo,
        String descripcion,
        String responsableAlt,
        PrioridadTarea prioridad,
        LocalDate fechaVencimiento
) {
}
