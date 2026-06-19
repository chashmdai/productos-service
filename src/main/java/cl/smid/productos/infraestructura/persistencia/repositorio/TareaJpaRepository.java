package cl.smid.productos.infraestructura.persistencia.repositorio;

import cl.smid.productos.infraestructura.persistencia.entidad.TareaEntidad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

/** Repositorio Spring Data de {@link TareaEntidad}. */
public interface TareaJpaRepository
        extends JpaRepository<TareaEntidad, Long>, JpaSpecificationExecutor<TareaEntidad> {

    /** Busca una Tarea por su identificador opaco. */
    Optional<TareaEntidad> findByAltKey(String altKey);

    /** Tareas de un Producto ordenadas por creacion (para embeber en el detalle). */
    List<TareaEntidad> findByIdProductoAltOrderByCreadoEnAscIdAsc(String idProductoAlt);

    /** Indica si ya existe una semilla para el Caso con el tipo indicado (idempotencia). */
    boolean existsByIdCasoAltAndTipoSemilla(String idCasoAlt, String tipoSemilla);
}
