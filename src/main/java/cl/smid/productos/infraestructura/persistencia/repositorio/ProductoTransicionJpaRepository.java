package cl.smid.productos.infraestructura.persistencia.repositorio;

import cl.smid.productos.infraestructura.persistencia.entidad.ProductoTransicionEntidad;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/** Repositorio Spring Data de los asientos de historial de Producto. */
public interface ProductoTransicionJpaRepository
        extends JpaRepository<ProductoTransicionEntidad, Long> {

    /** Asientos de un Producto ordenados cronologicamente. */
    List<ProductoTransicionEntidad> findByProductoIdOrderByOcurridoEnAscIdAsc(Long productoId);
}
