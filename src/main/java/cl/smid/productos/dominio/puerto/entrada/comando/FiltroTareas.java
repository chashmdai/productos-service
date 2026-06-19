package cl.smid.productos.dominio.puerto.entrada.comando;

import cl.smid.productos.dominio.modelo.ContextoSesion;
import cl.smid.productos.dominio.modelo.EstadoTarea;

/**
 * Criterios de listado de Tareas. Combina filtros opcionales con el contexto de sesion, que el
 * adaptador de persistencia traduce en un predicado territorial (override 6). Los campos nulos no
 * filtran.
 *
 * @param contexto      contexto de sesion del solicitante (obligatorio).
 * @param idCasoAlt     filtro por Caso (opcional).
 * @param idProductoAlt filtro por Producto padre (opcional).
 * @param responsableAlt filtro por responsable (opcional).
 * @param estado        filtro por estado (opcional).
 * @param pagina        indice de pagina base 0.
 * @param tamano        tamano de pagina (mayor que 0).
 */
public record FiltroTareas(
        ContextoSesion contexto,
        String idCasoAlt,
        String idProductoAlt,
        String responsableAlt,
        EstadoTarea estado,
        int pagina,
        int tamano
) {
}
