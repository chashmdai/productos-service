package cl.smid.productos.infraestructura.persistencia.repositorio;

import cl.smid.productos.infraestructura.persistencia.entidad.TareaTransicionEntidad;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/** Repositorio Spring Data de los asientos de historial de Tarea. */
public interface TareaTransicionJpaRepository
        extends JpaRepository<TareaTransicionEntidad, Long> {

    /** Asientos de una Tarea ordenados cronologicamente. */
    List<TareaTransicionEntidad> findByTareaIdOrderByOcurridoEnAscIdAsc(Long tareaId);
}
