package cl.smid.productos.dominio.puerto.entrada.comando;

import cl.smid.productos.dominio.modelo.ContextoSesion;
import cl.smid.productos.dominio.modelo.EstadoProducto;
import cl.smid.productos.dominio.modelo.TipoProducto;

/**
 * Criterios de listado de Productos. Combina filtros opcionales con el contexto de sesion, que el
 * adaptador de persistencia traduce en un predicado territorial (override 6). Los campos nulos no
 * filtran.
 *
 * @param contexto contexto de sesion del solicitante (obligatorio; aporta alcance/sede/unidad).
 * @param idCasoAlt filtro por Caso (opcional).
 * @param estado    filtro por estado (opcional).
 * @param tipo      filtro por tipo (opcional).
 * @param pagina    indice de pagina base 0.
 * @param tamano    tamano de pagina (mayor que 0).
 */
public record FiltroProductos(
        ContextoSesion contexto,
        String idCasoAlt,
        EstadoProducto estado,
        TipoProducto tipo,
        int pagina,
        int tamano
) {
}
