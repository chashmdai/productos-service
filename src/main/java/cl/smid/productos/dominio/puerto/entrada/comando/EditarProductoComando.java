package cl.smid.productos.dominio.puerto.entrada.comando;

import cl.smid.productos.dominio.modelo.ContextoSesion;

/**
 * Comando de edicion parcial de un Producto (merge de campos no nulos). Solo aplicable en estados
 * mutables (BORRADOR/EN_REVISION); la validacion la realiza el orquestador.
 *
 * @param contexto    contexto de sesion del solicitante (obligatorio).
 * @param altKey      identificador opaco del Producto a editar (obligatorio).
 * @param titulo      nuevo titulo (opcional).
 * @param descripcion nueva descripcion (opcional).
 */
public record EditarProductoComando(
        ContextoSesion contexto,
        String altKey,
        String titulo,
        String descripcion
) {
}
