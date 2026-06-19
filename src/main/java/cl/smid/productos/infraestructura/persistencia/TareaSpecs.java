package cl.smid.productos.infraestructura.persistencia;

import cl.smid.productos.dominio.puerto.entrada.comando.FiltroTareas;
import cl.smid.productos.infraestructura.persistencia.entidad.TareaEntidad;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

/**
 * Construccion de {@link Specification} para el listado de Tareas. Reutiliza el predicado
 * territorial de {@link ProductoSpecs} y agrega los filtros opcionales propios de la Tarea.
 */
public final class TareaSpecs {

    private TareaSpecs() {
    }

    public static Specification<TareaEntidad> desdeFiltro(FiltroTareas filtro) {
        return (root, query, cb) -> {
            List<Predicate> predicados = new ArrayList<>();

            if (tieneTexto(filtro.idCasoAlt())) {
                predicados.add(cb.equal(root.get("idCasoAlt"), filtro.idCasoAlt()));
            }
            if (tieneTexto(filtro.idProductoAlt())) {
                predicados.add(cb.equal(root.get("idProductoAlt"), filtro.idProductoAlt()));
            }
            if (tieneTexto(filtro.responsableAlt())) {
                predicados.add(cb.equal(root.get("responsableAlt"), filtro.responsableAlt()));
            }
            if (filtro.estado() != null) {
                predicados.add(cb.equal(root.get("estado"), filtro.estado().name()));
            }

            ProductoSpecs.agregarTerritorio(predicados, filtro.contexto(), root, cb);

            return cb.and(predicados.toArray(new Predicate[0]));
        };
    }

    private static boolean tieneTexto(String s) {
        return s != null && !s.isBlank();
    }
}
