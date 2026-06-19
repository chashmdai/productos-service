package cl.smid.productos.infraestructura.persistencia;

import cl.smid.productos.dominio.modelo.ContextoSesion;
import cl.smid.productos.dominio.puerto.entrada.comando.FiltroProductos;
import cl.smid.productos.infraestructura.persistencia.entidad.ProductoEntidad;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

/**
 * Construccion de {@link Specification} para el listado de Productos. Combina los filtros
 * opcionales del {@link FiltroProductos} con el predicado territorial derivado del alcance del
 * contexto (override 6): NACIONAL no restringe, SEDE acota por sede, UNIDAD acota por unidad.
 */
public final class ProductoSpecs {

    private ProductoSpecs() {
    }

    public static Specification<ProductoEntidad> desdeFiltro(FiltroProductos filtro) {
        return (root, query, cb) -> {
            List<Predicate> predicados = new ArrayList<>();

            if (tieneTexto(filtro.idCasoAlt())) {
                predicados.add(cb.equal(root.get("idCasoAlt"), filtro.idCasoAlt()));
            }
            if (filtro.estado() != null) {
                predicados.add(cb.equal(root.get("estado"), filtro.estado().name()));
            }
            if (filtro.tipo() != null) {
                predicados.add(cb.equal(root.get("tipo"), filtro.tipo().name()));
            }

            agregarTerritorio(predicados, filtro.contexto(), root, cb);

            return cb.and(predicados.toArray(new Predicate[0]));
        };
    }

    static void agregarTerritorio(List<Predicate> predicados, ContextoSesion ctx,
                                  jakarta.persistence.criteria.Root<?> root,
                                  jakarta.persistence.criteria.CriteriaBuilder cb) {
        switch (ctx.alcance()) {
            case NACIONAL -> { /* sin restriccion territorial */ }
            case SEDE -> predicados.add(cb.equal(root.get("idSedeAlt"), ctx.idSede()));
            case UNIDAD -> predicados.add(cb.equal(root.get("idUnidadAlt"), ctx.idUnidad()));
        }
    }

    private static boolean tieneTexto(String s) {
        return s != null && !s.isBlank();
    }
}
