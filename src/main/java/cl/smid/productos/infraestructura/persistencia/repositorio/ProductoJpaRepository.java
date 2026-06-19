package cl.smid.productos.infraestructura.persistencia.repositorio;

import cl.smid.productos.infraestructura.persistencia.entidad.ProductoEntidad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

/**
 * Repositorio Spring Data de {@link ProductoEntidad}. La capacidad de especificaciones habilita
 * el listado con filtros opcionales combinados con el predicado territorial.
 */
public interface ProductoJpaRepository
        extends JpaRepository<ProductoEntidad, Long>, JpaSpecificationExecutor<ProductoEntidad> {

    /** Busca un Producto por su identificador opaco. */
    Optional<ProductoEntidad> findByAltKey(String altKey);
}
