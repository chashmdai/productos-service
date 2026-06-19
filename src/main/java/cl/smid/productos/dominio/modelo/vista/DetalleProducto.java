package cl.smid.productos.dominio.modelo.vista;

import cl.smid.productos.dominio.modelo.Producto;
import cl.smid.productos.dominio.modelo.Tarea;

import java.util.List;

/**
 * Vista de detalle de un {@link Producto} junto con las {@link Tarea} que cuelgan de el.
 *
 * <p>Es la unidad de respuesta de todas las operaciones de Producto: en la creacion la lista
 * de tareas viene vacia; en la lectura de detalle se embeben las tareas hijas para evitar que
 * el cliente haga una segunda llamada. La composicion se arma en el orquestador (servicio de
 * dominio), no en la entidad.</p>
 *
 * @param producto producto raiz.
 * @param tareas   tareas asociadas al producto (puede ser vacia, nunca nula).
 */
public record DetalleProducto(
        Producto producto,
        List<Tarea> tareas
) {
    public DetalleProducto {
        tareas = tareas == null ? List.of() : List.copyOf(tareas);
    }

    /** Detalle de un producto recien creado, sin tareas. */
    public static DetalleProducto sinTareas(Producto producto) {
        return new DetalleProducto(producto, List.of());
    }
}
