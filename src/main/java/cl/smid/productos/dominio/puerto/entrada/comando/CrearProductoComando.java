package cl.smid.productos.dominio.puerto.entrada.comando;

import cl.smid.productos.dominio.modelo.ContextoSesion;
import cl.smid.productos.dominio.modelo.TipoProducto;

/**
 * Comando de creacion de un Producto. La sede y la unidad no se reciben: se heredan del Caso
 * (enriquecimiento activo) o del contexto del usuario (defecto).
 *
 * @param contexto    contexto de sesion del autor (obligatorio).
 * @param idCasoAlt   Caso al que pertenece el Producto (obligatorio).
 * @param tipo        tipo de entregable (obligatorio).
 * @param titulo      titulo del Producto (obligatorio).
 * @param descripcion descripcion (opcional).
 */
public record CrearProductoComando(
        ContextoSesion contexto,
        String idCasoAlt,
        TipoProducto tipo,
        String titulo,
        String descripcion
) {
}
