package cl.smid.productos.dominio.puerto.salida;

import cl.smid.productos.dominio.modelo.Producto;
import cl.smid.productos.dominio.modelo.vista.PaginaDominio;
import cl.smid.productos.dominio.puerto.entrada.comando.FiltroProductos;

import java.util.Optional;

/**
 * Puerto de persistencia del agregado {@link Producto}.
 *
 * <p>Las operaciones de escritura persisten tanto el estado del agregado como sus transiciones
 * pendientes ({@link Producto#transicionesPendientes()}) de forma atomica. La transaccionalidad
 * la gobierna la frontera (controlador); este puerto no abre transacciones propias.</p>
 */
public interface RepositorioProductos {

    /** Inserta un Producto nuevo junto con su asiento de creacion. */
    void crear(Producto producto);

    /** Persiste los cambios de un Producto existente e inserta sus transiciones pendientes. */
    void actualizar(Producto producto);

    /**
     * Recupera un Producto por su identificador opaco, con su historial completo. El filtrado
     * territorial es responsabilidad del orquestador (que compara sede/unidad con el contexto).
     */
    Optional<Producto> buscarPorAltKey(String altKey);

    /** Lista Productos aplicando los filtros y el predicado territorial derivado del contexto. */
    PaginaDominio<Producto> listar(FiltroProductos filtro);
}
