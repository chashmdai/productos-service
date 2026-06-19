package cl.smid.productos.dominio.puerto.entrada;

import cl.smid.productos.dominio.modelo.ContextoSesion;
import cl.smid.productos.dominio.modelo.Producto;
import cl.smid.productos.dominio.modelo.vista.DetalleProducto;
import cl.smid.productos.dominio.modelo.vista.PaginaDominio;
import cl.smid.productos.dominio.puerto.entrada.comando.CrearProductoComando;
import cl.smid.productos.dominio.puerto.entrada.comando.EditarProductoComando;
import cl.smid.productos.dominio.puerto.entrada.comando.FiltroProductos;
import cl.smid.productos.dominio.puerto.entrada.comando.TransicionProductoComando;

/**
 * Puerto de entrada para la gestion de Productos (caso de uso). Lo implementa el orquestador de
 * dominio. Todas las respuestas de mutacion devuelven {@link DetalleProducto} para que el cliente
 * disponga del estado completo (con tareas, vacias tras crear) sin una segunda llamada.
 */
public interface GestionProductos {

    /** Crea un Producto en BORRADOR heredando sede/unidad y publicando {@code producto.creado}. */
    DetalleProducto crear(CrearProductoComando comando);

    /** Detalle de un Producto (con sus tareas) sujeto a alcance territorial. */
    DetalleProducto detalle(ContextoSesion contexto, String altKey);

    /** Listado paginado de Productos segun filtros y alcance territorial. */
    PaginaDominio<Producto> listar(FiltroProductos filtro);

    /** Edita un Producto en estado mutable (merge de campos no nulos). */
    DetalleProducto editar(EditarProductoComando comando);

    /** Aplica una transicion de estado (ENVIAR_REVISION, EMITIR, ANULAR) con sus reglas de rol. */
    DetalleProducto transicionar(TransicionProductoComando comando);
}
