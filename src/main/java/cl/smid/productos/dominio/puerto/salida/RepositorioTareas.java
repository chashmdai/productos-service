package cl.smid.productos.dominio.puerto.salida;

import cl.smid.productos.dominio.modelo.Tarea;
import cl.smid.productos.dominio.modelo.vista.PaginaDominio;
import cl.smid.productos.dominio.puerto.entrada.comando.FiltroTareas;

import java.util.List;
import java.util.Optional;

/**
 * Puerto de persistencia del agregado {@link Tarea}.
 *
 * <p>Analogo a {@link RepositorioProductos}: las escrituras persisten el agregado y sus
 * transiciones pendientes de forma atomica, sin abrir transacciones propias.</p>
 */
public interface RepositorioTareas {

    /** Inserta una Tarea nueva junto con su asiento de creacion. */
    void crear(Tarea tarea);

    /** Persiste los cambios de una Tarea existente e inserta sus transiciones pendientes. */
    void actualizar(Tarea tarea);

    /** Recupera una Tarea por su identificador opaco, con su historial completo. */
    Optional<Tarea> buscarPorAltKey(String altKey);

    /** Lista Tareas aplicando los filtros y el predicado territorial derivado del contexto. */
    PaginaDominio<Tarea> listar(FiltroTareas filtro);

    /** Tareas asociadas a un Producto (para embeberlas en el detalle), sin paginar, sin historial. */
    List<Tarea> listarPorProducto(String idProductoAlt);

    /**
     * Indica si ya existe una Tarea sembrada para el Caso con el tipo de semilla dado. Soporta la
     * idempotencia del sembrado por evento (unicidad {@code (id_caso_alt, tipo_semilla)}).
     */
    boolean existeSemilla(String idCasoAlt, String tipoSemilla);
}
